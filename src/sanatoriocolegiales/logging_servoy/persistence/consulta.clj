(ns sanatoriocolegiales.logging-servoy.persistence.consulta
  (:require [datomic.api :as d]
            [clojure.instant :refer [read-instant-date]]))

(def excepcion-desde-fecha '[:find (pull ?e [* {:evento/origen [:db/ident]} {:paciente/tipo [:db/ident]}])
                             :in $ ?fecha
                             :where
                             [?e :evento/fecha ?f]
                             [(>= ?f ?fecha)]])

(def excepcion-por-origen '[:find (pull ?e [:paciente/historia-clinica-unica
                                            :paciente/historia-clinica
                                            :evento/nombre
                                            :evento/fecha
                                            {:evento/estado [:estado/excepcion :estado/ok]}
                                            :evento/excepcion
                                            {:paciente/tipo [:db/ident]}])
                            :in $ ?origen
                            :where
                            [?e :evento/origen ?origen]])

(def origenes-de-eventos '[:find  [?nombre ...]
                           :where [_ :evento/origen ?e]
                           [?e :db/ident ?nombre]])

(def evento-por-hc '[:find (pull ?e [:paciente/historia-clinica-unica
                                     :evento/nombre
                                     :evento/fecha
                                     {:evento/estado [:estado/excepcion :estado/ok]}
                                     :evento/excepcion
                                     {:evento/origen [:db/ident]}
                                     {:paciente/tipo [:db/ident]}])
                     :in $ ?h
                     :where
                     [?e :paciente/historia-clinica ?h]])

(def evento-por-hcu '[:find (pull ?e [:paciente/historia-clinica
                                      :evento/nombre
                                      :evento/fecha
                                      {:evento/estado [:estado/excepcion :estado/ok]}
                                      :evento/excepcion
                                      {:evento/origen [:db/ident]}
                                      {:paciente/tipo [:db/ident]}])
                      :in $ ?h
                      :where
                      [?e :paciente/historia-clinica-unica ?h]])

(def evento-por-patron-de-nombre)
 
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
  [db hc]
  (cond
    (nil? db) (throw (IllegalArgumentException. "La instancia de la base de datos es nula"))
    (or (not hc) (not (number? hc))) (throw (IllegalArgumentException. "El hc debe ser un número"))
    :else
    (d/q evento-por-hc db hc)))

(defn buscar-eventos-por-historia-clinica-unica
  [db hcu]
  (cond
    (nil? db) (throw (IllegalArgumentException. "La instancia de la base de datos es nula"))
    (or (not hcu) (not (number? hcu))) (throw (IllegalArgumentException. "El hcu debe ser un número"))
    :else
    (d/q evento-por-hcu db hcu)))

(defn buscar-por-patron-nombre-de-evento
  [db patron]
  (cond
    (nil? db) (throw (IllegalArgumentException. "La instancia de la base de datos es nula"))
    (not patron) (throw (IllegalArgumentException. "El patrón regex no puede ser nulo"))
    :else
    (d/q evento-por-patron-de-nombre db patron)))

(defn obtener-origenes-eventos
  [db]
  (cond
    (nil? db) (throw (IllegalArgumentException. "La instancia de la base de datos es nula")) 
    :else
    (d/q origenes-de-eventos db)))