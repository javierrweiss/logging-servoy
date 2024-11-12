(ns sanatoriocolegiales.logging-servoy.persistence.datomic.consulta
  (:require [datomic.api :as d]))

(def excepcion-desde-fecha '[:find (pull ?e [* {:evento/origen [:db/ident]} {:paciente/tipo [:db/ident]}])
                             :in $ ?fecha
                             :where
                             [?e :evento/fecha ?f]
                             [(>= ?f ?fecha)]
                             [?e :evento/estado ?est]
                             [?est :estado/excepcion _]])

(def excepcion-por-origen '[:find (pull ?e [:evento/id
                                            :paciente/historia_clinica_unica
                                            :paciente/historia_clinica
                                            :evento/nombre
                                            :evento/fecha
                                            {:evento/estado [:estado/excepcion :estado/ok]}
                                            :evento/excepcion
                                            {:paciente/tipo [:db/ident]}])
                            :in $ ?origen
                            :where
                            [?e :evento/origen ?origen]
                            [?e :evento/estado ?est]
                            [?est :estado/excepcion _]])

(def origenes-de-eventos '[:find  [?nombre ...]
                           :where [_ :evento/origen ?e]
                           [?e :db/ident ?nombre]])

(def evento-por-hc '[:find (pull ?e [:evento/id
                                     :paciente/historia_clinica_unica
                                     :evento/nombre
                                     :evento/fecha
                                     {:evento/estado [:estado/excepcion :estado/ok]}
                                     :evento/excepcion
                                     {:evento/origen [:db/ident]}
                                     {:paciente/tipo [:db/ident]}])
                     :in $ ?h
                     :where
                     [?e :paciente/historia_clinica ?h]])

(def evento-por-hcu '[:find (pull ?e [:evento/id
                                      :paciente/historia_clinica
                                      :evento/nombre
                                      :evento/fecha
                                      {:evento/estado [:estado/excepcion :estado/ok]}
                                      :evento/excepcion
                                      {:evento/origen [:db/ident]}
                                      {:paciente/tipo [:db/ident]}])
                      :in $ ?h
                      :where
                      [?e :paciente/historia_clinica_unica ?h]])

(def evento-por-patron-de-nombre '[:find (pull ?e [* {:evento/origen [:db/ident]}
                                                   {:paciente/tipo [:db/ident]}])
                                   :in $ ?evento
                                   :where
                                   [?e :evento/nombre ?nombre]
                                   [(re-seq ?evento ?nombre)]])

(defn obtener-por-id
  [db id]
  (when-not db 
    (throw (ex-info "La instancia de la base de datos es nula"
                    {:type :sanatoriocolegiales.logging-servoy.middleware/excepcion-persistencia})))
  (d/q '[:find (pull ?e [* 
                          {:evento/origen [:db/ident]}
                          {:evento/estado [:db/ident :estado/excepcion :estado/ok]}
                          {:paciente/tipo [:db/ident]}])
         :in $ ?id
         :where [?e :evento/id ?id]]
       db
       id))
 
(defn buscar-excepcion-desde
  [db fecha]
  (cond
    (nil? db) (throw (ex-info "La instancia de la base de datos es nula"
                              {:type :sanatoriocolegiales.logging-servoy.middleware/excepcion-persistencia}))
    (not fecha) (throw (ex-info "Debe introducir una fecha en formato Instant"
                                {:type :sanatoriocolegiales.logging-servoy.middleware/argumento-ilegal}))
    (not (inst? fecha)) (throw (ex-info "La fecha debe estar en formato Instant"
                                          {:type :sanatoriocolegiales.logging-servoy.middleware/argumento-ilegal}))
    :else 
    (d/q excepcion-desde-fecha db fecha)))
 
(defn buscar-excepcion-por-origen
  [db origen]
  (cond 
    (nil? db) (throw (ex-info "La instancia de la base de datos es nula"
                              {:type :sanatoriocolegiales.logging-servoy.middleware/excepcion-persistencia}))
    (or (not origen) (not (qualified-keyword? origen))) (throw (ex-info "El origen debe ser un keyword calificado ajustado al esquema, e.g. :evento/uco, :evento/convenios, etc."
                                                                        {:type :sanatoriocolegiales.logging-servoy.middleware/argumento-ilegal}))
    :else
        (d/q excepcion-por-origen db origen)))
 
(defn buscar-eventos-por-historia-clinica
  [db hc]
  (cond
    (nil? db) (throw (ex-info "La instancia de la base de datos es nula"
                              {:type :sanatoriocolegiales.logging-servoy.middleware/excepcion-persistencia}))
    (or (not hc) (not (number? hc))) (throw (ex-info "El hc debe ser un número"
                                                     {:type :sanatoriocolegiales.logging-servoy.middleware/argumento-ilegal}))
    :else
    (d/q evento-por-hc db hc)))

(defn buscar-eventos-por-historia-clinica-unica
  [db hcu]
  (cond
    (nil? db) (throw (ex-info "La instancia de la base de datos es nula"
                              {:type :sanatoriocolegiales.logging-servoy.middleware/excepcion-persistencia}))
    (or (not hcu) (not (number? hcu))) (throw (ex-info "El hcu debe ser un número"
                                                       {:type :sanatoriocolegiales.logging-servoy.middleware/argumento-ilegal}))
    :else
    (d/q evento-por-hcu db hcu)))

(defn obtener-origenes-eventos
  [db]
  (cond
    (nil? db) (throw (ex-info "La instancia de la base de datos es nula"
                              {:type :sanatoriocolegiales.logging-servoy.middleware/excepcion-persistencia})) 
    :else
    (d/q origenes-de-eventos db)))

(defn buscar-eventos-por-patron-de-nombre
  [db patron]
  (cond
    (nil? db) (throw (ex-info "La instancia de la base de datos es nula"
                              {:type :sanatoriocolegiales.logging-servoy.middleware/excepcion-persistencia}))
    (not (string? patron)) (throw (ex-info "El patrón de búsqueda debe ser un string"
                                           {:type :sanatoriocolegiales.logging-servoy.middleware/argumento-ilegal}))
    :else
    (let [patron (re-pattern (str "(?ix)" patron))]
      (d/q evento-por-patron-de-nombre db patron))))

(defn obtener-todo
  [db]
  (cond
    (nil? db) (throw (ex-info "La instancia de la base de datos es nula"
                              {:type :sanatoriocolegiales.logging-servoy.middleware/excepcion-persistencia}))
    :else
    (d/q '[:find (pull ?e [*
                           {:evento/estado [:estado/excepcion :estado/ok]}
                           {:evento/origen [:db/ident]}
                           {:paciente/tipo [:db/ident]}])
           :where
           [?e :evento/nombre _]]
         db)))


(comment
  (def conn (-> (:donut.system/instances (system-repl/system))
                :db
                :datomic))
  (def db (d/db conn))
  (tap> (buscar-eventos-por-historia-clinica db 21100)) 
  (tap> (buscar-eventos-por-historia-clinica-unica db 295550))
  (tap> (buscar-eventos-por-patron-de-nombre db "CONV"))
  (tap> (obtener-origenes-eventos db))   
  (tap> (buscar-excepcion-por-origen db :origen/cirugia))
  (tap> (buscar-excepcion-desde db "2024-05-14")) 
   (tap> (obtener-por-id db 17592186045432)) 
    
  :rcf) 