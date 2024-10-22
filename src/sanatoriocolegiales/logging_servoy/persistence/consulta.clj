(ns sanatoriocolegiales.logging-servoy.persistence.consulta
  (:require [datomic.api :as d]
            [clojure.instant :refer [read-instant-date]]))

(def excepcion-desde-fecha '[:find ?e ?evento ?origen ?excepcion ?hc
                             :keys id evento origen excepcion historia-clinica
                             :in $ $fecha
                             :where
                             [$fecha ?e :evento/nombre ?evento]
                             [$ ?e :evento/origen ?origen]
                             [$ ?e :paciente/historia-clinica ?hc]
                             [$ ?e :evento/excepcion ?excepcion]])

(def excepcion-por-origen '[:find ?e ?evento ?hc ?hcu ?excepcion ?estado ?tx
                            :keys id evento hc hcu excepcion registro fecha
                            :in $ ?origen
                            :where
                            [?e :evento/origen ?origen ?tx]
                            [?e :evento/nombre ?evento ?tx]
                            [?e :paciente/historia-clinica ?hc ?tx]
                            [?e :paciente/historia-clinica-unica ?hcu ?tx]
                            [?e :evento/excepcion ?excepcion ?tx]
                            [?e :evento/estado ?estado ?tx]])
 
(defn buscar-excepcion-desde
  [db fecha]
  (cond
    (nil? db) (throw (IllegalArgumentException. "La instancia de la base de datos es nula"))
    (not fecha) (throw (IllegalArgumentException. "Debe introducir una fecha en formato string"))
    (not (string? fecha)) (throw (IllegalArgumentException. "La fecha debe estar en formato string"))
    :else
    (let [f (read-instant-date fecha)]
      (d/q excepcion-desde-fecha db f))))

(defn buscar-excepcion-por-origen
  [db origen]
  (cond 
    (nil? db) (throw (IllegalArgumentException. "La instancia de la base de datos es nula"))
    (or (not origen) (not (qualified-keyword? origen))) (throw (IllegalArgumentException. "El origen debe ser un keyword calificado ajustado al esquema, e.g. :evento/uco, :evento/convenios, etc."))
    :else
        (d/q excepcion-por-origen db origen)))
 
(defn buscar-eventos-por-historia-clinica
  [])

(defn buscar-eventos-por-historia-clinica-unica
  [])