(ns sanatoriocolegiales.logging-servoy.persistence.datomic.transaccion
  (:require [datomic.api :as d]
            [sanatoriocolegiales.logging-servoy.persistence.datomic.esquema :refer [log-schema]]
            [com.brunobonacci.mulog :as µ]))

(defn registrar-esquema!
  [conn]
  (when-not conn
    (throw (ex-info 
            "No existe conexión a la base de datos" 
            {:sanatoriocolegiales.logging-servoy.middleware/excepcion-persistencia "No existe conexión a la base de datos"})))
  (try
    @(d/transact conn log-schema)
    (catch Exception e (let [msj (ex-message e)] 
                         (µ/log ::error-al-registrar-esquema :mensaje msj)
                         (throw (ex-info
                                 "Hubo un error al persistir el esquema"
                                 {:sanatoriocolegiales.logging-servoy.middleware/excepcion-persistencia "Hubo un error al persistir el esquema"
                                  :mensaje msj}))))))

(defn ejecutar!
  [conn datos]
  (when-not conn
    (throw (ex-info
            "No existe conexión a la base de datos"
            {:sanatoriocolegiales.logging-servoy.middleware/excepcion-persistencia "No existe conexión a la base de datos"})))
  (try
    @(d/transact conn datos)
    (catch Exception e (let [msj (ex-message e)]
                         (µ/log ::error-al-ejecutar-transaccion :mensaje (ex-message e) :datos datos)
                         (throw (ex-info
                                 "Hubo un error al persistir los datos"
                                 {:sanatoriocolegiales.logging-servoy.middleware/excepcion-persistencia "Hubo un error al persistir el esquema"
                                  :datos datos
                                  :mensaje msj}))))))

(defn obtener-estado-db!
  [conn]
  (when-not conn
    (throw (ex-info
            "No existe conexión a la base de datos"
            {:sanatoriocolegiales.logging-servoy.middleware/excepcion-persistencia "No existe conexión a la base de datos"})))
  (try
    (d/db conn)
    (catch Exception e (let [msj (ex-message e)]
                         (µ/log ::error-al-obtener-db :mensaje (ex-message e))
                         (throw (ex-info
                                 "Hubo un error al persistir los datos"
                                 {:sanatoriocolegiales.logging-servoy.middleware/excepcion-persistencia "Hubo un error al persistir el esquema" 
                                  :mensaje msj}))))))

(defn actualizar-atributo-esquema
  "Devuelve un mapa que permite actualizar un atributo en el esquema de la base de datos"
  [ident-anterior ident-nuevo]
  (if-not (and (qualified-keyword? ident-anterior) (qualified-keyword? ident-nuevo))
    (throw (ex-info "Los argumentos deben ser keywords calificados que se correspondan con los atributos de la base de datos"
                    {:sanatoriocolegiales.logging-servoy.middleware/argumento-ilegal "Los argumentos deben ser keywords calificados que se correspondan con los atributos de la base de datos"
                     :argumentos [ident-anterior ident-nuevo]}))
    {:db/id ident-anterior
     :db/ident ident-nuevo}))

(defn agregar-nuevo-atributo
  [ident tipo-dato cardinalidad doc unico?]
  (let [tipos-permitidos #{:db.type/bigdec
                           :db.type/bigint
                           :db.type/boolean
                           :db.type/bytes
                           :db.type/double
                           :db.type/float
                           :db.type/instant
                           :db.type/keyword
                           :db.type/long
                           :db.type/ref
                           :db.type/string
                           :db.type/symbol
                           :db.type/tuple
                           :db.type/uuid
                           :db.type/uri}]
    (cond
      (not (qualified-keyword? ident))
      (throw (ex-info "El ident debe ser un keyword calificado" 
                      {:sanatoriocolegiales.logging-servoy.middleware/argumento-ilegal "El ident debe ser un keyword calificado"}))
      (not (some tipos-permitidos [tipo-dato]))
      (throw (ex-info "tipo-dato debe ser un keyword entre los permitidos https://docs.datomic.com/schema/schema-reference.html#db-valuetype"
                      {:sanatoriocolegiales.logging-servoy.middleware/argumento-ilegal "tipo-dato debe ser un keyword entre los permitidos https://docs.datomic.com/schema/schema-reference.html#db-valuetype"}))
      (not (some #{:db.cardinality/one :db.cardinality/many} [cardinalidad]))
      (throw (ex-info "cardinalidad debe ser :db.cardinality/one ó :db.cardinality/many"
                      {:sanatoriocolegiales.logging-servoy.middleware/argumento-ilegal "cardinalidad debe ser :db.cardinality/one ó :db.cardinality/many"}))
      (or (not doc) (not (string? doc)))
      (throw (ex-info "doc debe ser un string"
                      {:sanatoriocolegiales.logging-servoy.middleware/argumento-ilegal "doc debe ser un string"}))
      (not (boolean? unico?))
      (throw (ex-info "unico? debe ser un booleano"
                      {:sanatoriocolegiales.logging-servoy.middleware/argumento-ilegal "unico? debe ser un booleano"}))
      :else (cond-> {:db/ident ident
                     :db/valueType tipo-dato
                     :db/cardinality cardinalidad
                     :db/doc doc}
              unico? (assoc :db/unique :db.unique/identity)))))
  
(comment

  (d/get-database-names "datomic:sql://*?jdbc:postgresql://10.200.0.190:5432/bases_auxiliares?user=auxiliar&password=auxi2013")

  (d/delete-database (System/getenv "DATOMIC"))

  (d/create-database (System/getenv "DATOMIC"))

  (registrar-esquema! cnn)

  (def cnn (-> (:donut.system/instances (system-repl/system))
               :db
               :datomic))
  
  (ejecutar! cnn [{:evento/origen {:db/ident :evento/uti},
                   :evento/nombre "EVENTO ACTUALIZADO OTRA VEZ",
                   :paciente/historia-clinica 55,
                   :paciente/historia-clinica-unica 11,
                   :evento/estado {:estado/ok ["ewewwewe"]},
                   :paciente/tipo {:db/ident :paciente/internado},
                   :evento/fecha #inst "2024-11-01T12:10:00.000-00:00"}])
  
  (ejecutar! cnn [{:db/excise 17592186045429}])

  (ejecutar! cnn [{:db/id    :paciente/historia-clinica
                   :db/ident :paciente/historia_clinica}
                  {:db/id    :paciente/historia-clinica-unica
                   :db/ident :paciente/historia_clinica_unica}
                  {:db/id    :convenios/nro-lote
                   :db/ident :convenios/nro_lote}
                  {:db/id    :convenios/contador-registros
                   :db/ident :convenios/contador_registros}])

  (def db (d/db cnn))

  (d/q '[:find (pull ?e [*])
         :where [?e :evento/origen :origen/cirugia]]
       db)

  (d/q '[:find (pull ?e [:paciente/historia-clinica-unica
                         :evento/nombre
                         :evento/fecha
                         {:evento/estado [:estado/excepcion :estado/ok]}
                         :evento/excepcion
                         {:evento/origen [:db/ident]}
                         {:paciente/tipo [:db/ident]}])
         :in $ ?h
         :where
         [?e :paciente/historia-clinica ?h]]
       db
       3167170)

  (d/q '[:find (pull ?e [:db/id
                         :paciente/historia-clinica-unica
                         :evento/nombre
                         :evento/fecha
                         {:evento/estado [:estado/excepcion :estado/ok]}
                         :evento/excepcion
                         {:evento/origen [:db/ident]}
                         {:paciente/tipo [:db/ident]}])
         :in $ ?h
         :where
         [?e :paciente/historia-clinica ?h]]
       db
       778889)

  (d/q '[:find (pull ?e [* {:evento/origen [:db/ident]}
                         {:paciente/tipo [:db/ident]}])
         :in $
         :where
         [?e :evento/nombre ?nombre]
         [(re-seq (re-pattern "PREOPERATORIA") ?nombre)]]
       db)

  (d/q '[:find (pull ?e [* {:evento/origen [:db/ident]}
                         {:paciente/tipo [:db/ident]}])
         :in $ ?evento
         :where
         [?e :evento/nombre ?nombre]
         [(re-seq ?evento ?nombre)]]
       db
       (re-pattern (str "(?ix)" "seguridad")))


  (d/q '[:find ?fecha
         :where [?e :evento/fecha ?fecha]]
       db)

  (d/q '[:find  ?e
         :keys id
         :where [?e :evento/nombre "Creación historia clínica"]]
       db)

  (d/q '[:find ?e ?nombre
         :where [_ :evento/origen ?e]
         [?e :db/ident ?nombre]]
       db)

  (d/q '[:find  [?nombre ...]
         :where [_ :evento/origen ?e]
         [?e :db/ident ?nombre]]
       db)

  (d/q '[:find ?e
         :where [_ :db/ident ?e]]
       db)

  (d/q '[:find [?e ...]
         :where [_ :paciente/historia-clinica ?e]]
       db)

;; Busca excepciones desde una fecha  
  (d/q '[:find ?hc ?evento ?origen ?tipo
        ;; :keys id evento origen historia-clinica
         :in $ ?fecha
         :where
         [?e :evento/nombre ?evento]
         [?e :evento/origen ?o]
         [?o :db/ident ?origen]
         [?e :paciente/tipo ?t]
         [?t :db/ident ?tipo]
         [?e :paciente/historia-clinica ?hc]
         #_[?e :evento/estado :estado/excepcion]
         [?e :evento/fecha ?f]
         [(< ?fecha ?f)]]
       db
       (clojure.instant/read-instant-date "2024-10-19"))

  (tap> (d/q '[:find ?hc ?hcu ?evento ?origen ?tipo ?estado ?f
               :in $ ?fecha
               :where
               [?e :evento/fecha ?f]
               [(>= ?f ?fecha)]
               [?e :evento/nombre ?evento]
               [?e :paciente/historia-clinica ?hc]
               [?e :paciente/historia-clinica-unica ?hcu]
               [?e :evento/origen ?o]
               [?o :db/ident ?origen]
               [?e :paciente/tipo ?t]
               [?t :db/ident ?tipo]
               [?e :evento/estado ?est]
               [?est :estado/excepcion ?estado]
               #_(or [?est :estado/excepcion ?estado]
                     [?est :estado/ok ?estado])]
             db
             (clojure.instant/read-instant-date "2024-01-19")))

  (tap> (d/q '[:find (pull ?e [* {:evento/origen [:db/ident]}
                               {:paciente/tipo [:db/ident]}])
               :in $ ?fecha
               :where
               [?e :evento/fecha ?f]
               [(>= ?f ?fecha)]]
             db
             (clojure.instant/read-instant-date "2024-03-19")))

;; Busca evento por origen

  (tap> (d/q '[:find ?evento ?fecha ?hc ?hcu ?origen ?tipo ?estado
               ;;:keys id evento hc hcu excepcion registro fecha
               :in $ ?origen
               :where
               [?e :evento/nombre ?evento]
               [?e :evento/fecha ?fecha]
               [?e :paciente/historia-clinica ?hc]
               [?e :paciente/historia-clinica-unica ?hcu]
               #_[?e :evento/estado ?est]
               #_[(get-else $ ?e :evento/estado :db/ident) ?est]
               (or
                [?est :estado/excepcion ?estado]
                [?est :estado/ok ?estado])
               [?e :evento/origen ?o]
               [?o :db/ident ?origen]
               [?e :paciente/tipo ?t]
               [?t :db/ident ?tipo]]
             db
             :evento/cirugia))

  (tap> (d/q '[:find (pull ?e [:paciente/historia-clinica-unica
                               :paciente/historia-clinica
                               :evento/nombre
                               :evento/fecha
                               {:evento/estado [:estado/excepcion :estado/ok]}
                               :evento/excepcion
                               {:paciente/tipo [:db/ident]}])
               :in $ ?origen
               :where
               [?e :evento/origen ?origen]
               [?e :evento/estado ?est]
               [?est :estado/excepcion _]]
             db
             :evento/cirugia))

  (tap> (d/q '[:find (pull ?e [*])
               :where [?e :evento/nombre]]
             db))

  (d/q '[:find ?e
         :where [?e :evento/nombre]]
       db)

  (def history (d/history db))

  (count (d/q '[:find ?hc ?hcu ?nombre ?estado ?fecha ?origen
                :in $ ?id
                :where
                [?id :evento/nombre ?nombre]
                [?id :evento/estado ?estado]
                [?id :evento/fecha ?fecha]
                [?id :evento/origen ?origen]
                [?id :paciente/historia-clinica ?hc]
                [?id :paciente/historia-clinica-unica ?hcu]]
              history
              17592186045429))

  (d/q '[:find ?tx (pull ?id [* {:evento/origen [:db/ident]} {:paciente/tipo [:db/ident]}])
         :in $ ?id
         :where [?id :evento/nombre _ ?tx]]
       db
       17592186045429)
 
  (d/q '[:find ?tx (pull ?id [* {:evento/origen [:db/ident]} {:paciente/tipo [:db/ident]}])
         :in $ ?id
         :where [?id :evento/nombre _ ?tx]]
       history
       17592186045429)

  (d/q '[:find (pull ?id [*
                          {:evento/origen [:db/ident]}
                          {:evento/estado [:estado/excepcion :estado/ok]}
                          {:paciente/tipo [:db/ident]}])
         :in $ ?id
         :where [?id :evento/nombre _]]
       db
       17592186045429)

  (d/pull db '[*] 17592186045429)


  (agregar-nuevo-atributo :chupa/cabras :db.type/string :db.cardinality/one "Cualquier vaina" false)
  (agregar-nuevo-atributo :chupa/cabras :db.type/string :db.cardinality/one "Cualquier vaina" true)
  (agregar-nuevo-atributo :chupa/cabras :db.type/string :db.cardinality "Cualquier vaina" false)
  (agregar-nuevo-atributo :chupa/cabras :string :db.cardinality/one "Cualquier vaina" false)
  (agregar-nuevo-atributo 'chancho/bendito :db.type/string :db.cardinality/one "Cualquier vaina" false)

  (type cnn)
  )