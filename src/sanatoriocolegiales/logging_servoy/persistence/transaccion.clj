(ns sanatoriocolegiales.logging-servoy.persistence.transaccion
  (:require [datomic.api :as d]
            [sanatoriocolegiales.logging-servoy.persistence.esquema :refer [log-schema]]
            [com.brunobonacci.mulog :as µ]))

(defn registrar-esquema!
  [conn]
  (when-not conn
    (throw (IllegalArgumentException. "No existe conexión a la base de datos")))
  (try
    @(d/transact conn log-schema)
    (catch Exception e (µ/log ::error-al-registrar-esquema :mensaje (ex-message e)))))

(defn ejecutar!
  [conn datos]
  (when-not conn
    (throw (IllegalArgumentException. "No existe conexión a la base de datos")))
  (try
    @(d/transact conn datos)
    (catch Exception e (µ/log ::error-al-ejecutar-transaccion :mensaje (ex-message e) :datos datos))))

(defn actualizar-atributo-esquema
  "Devuelve un mapa que permite actualizar un atributo en el esquema de la base de datos"
  [ident-anterior ident-nuevo] 
  (if-not (and (qualified-keyword? ident-anterior) (qualified-keyword? ident-nuevo))
    (throw (IllegalArgumentException. "Los argumentos deben ser keywords calificados que se correspondan con los atributos de la base de datos"))
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
      (throw (IllegalArgumentException. "El ident debe ser un keyword calificado"))
      (not (some tipos-permitidos [tipo-dato]))
      (throw (IllegalArgumentException. "tipo-dato debe ser un keyword entre los permitidos https://docs.datomic.com/schema/schema-reference.html#db-valuetype"))
      (not (some #{:db.cardinality/one :db.cardinality/many} [cardinalidad]))
      (throw (IllegalArgumentException. "cardinalidad debe ser :db.cardinality/one ó :db.cardinality/many"))
      (or (not doc) (not (string? doc)))
      (throw (IllegalArgumentException. "doc debe ser un string"))
      (not (boolean? unico?))
      (throw (IllegalArgumentException. "unico? debe ser un booleano"))
      :else (cond-> {:db/ident ident
                     :db/valueType tipo-dato
                     :db/cardinality cardinalidad
                     :db/doc doc}
              unico? (assoc :db/unique :db.unique/identity)))))

(defn obtener-estado-db!
  [conn]
  (when-not conn
    (throw (IllegalArgumentException. "No existe conexión a la base de datos")))
  (try
    (d/db conn)
    (catch Exception e (µ/log ::error-al-obtener-db :mensaje (ex-message e)))))
  
(comment

  (def cnn (-> (:donut.system/instances (system-repl/system))
               :env
               :persistence
               :conn))
  
  ;; Actualizamos el ident de algunos atributos en el esquema
  (ejecutar! cnn [{:db/id :event/origin
                   :db/ident :evento/origen}
                  {:db/id :event/name
                   :db/ident :evento/nombre}])
  
  ;; Añadimos otros ident al esquema
  (ejecutar! cnn [{:db/ident :paciente/tipo
                   :db/valueType :db.type/ref
                   :db/cardinality :db.cardinality/one
                   :db/doc "Resuelve a un tipo de referencia :paciente/internado o :paciente/ambulatorio"}
                  {:db/ident :paciente/internado}
                  {:db/ident :paciente/ambulatorio}])
  
  (registrar-esquema! cnn)

  (ejecutar! cnn [{:evento/origen :evento/cirugia
                   :evento/nombre "Creación historia clínica internado"
                   :paciente/historia-clinica 2110902
                   :paciente/historia-clinica-unica 100999
                   :evento/estado "Chasm"
                   :evento/excepcion "ohh error!!!"}
                  {:evento/origen :evento/cirugia
                   :evento/nombre "Creación historia clínica ambulatorio"
                   :paciente/historia-clinica 2311300
                   :paciente/historia-clinica-unica 11450045
                   :evento/estado "Bla bla"
                   :evento/excepcion "Caracha!! Algo sucedió"}])

  (def db (d/db cnn))

  (d/q '[:find ?e
         :where [?e :evento/nombre "Creación historia clínica"]]
       db)

  (d/q '[:find ?e
         :where [_ :evento/nombre ?e]]
       db)

  (d/q '[:find ?e
         :where [_ :db/ident ?e]]
       db)

  ;; Esta consulta sigue respondiendo incluso con los viejos idents
  (d/q '[:find  ?e ?nombreevento ?origen-cod ?hc ?estado
         :keys  e nombreevento origen-cod hc estado
         :where 
         [?e :event/name ?nombreevento]
         [?e :event/origin ?origen-cod]
         [?e :paciente/historia-clinica ?hc]
         [?e :evento/estado ?estado]]
       db)

(d/q '[:find  ?e ?nombreevento ?origen-cod ?hc ?estado
       :keys  e nombreevento origen-cod hc estado
       :where
       [?e :evento/nombre ?nombreevento]
       [?e :evento/origen ?origen-cod]
       [?e :paciente/historia-clinica ?hc]
       [?e :evento/estado ?estado]]
     db)
  
  (d/pull db '[*] 17592186045429)

  (d/q '[:find ?hc ?nombreevento ?origen-cod ?estado
         :where
         [?hc :paciente/historia-clinica 1000]
         [?hc :event/name ?nombreevento]
         [?hc :event/origin ?origen-cod] 
         [?hc :evento/estado ?estado]]
       db)

  (d/q '[:find (d/pull ?e [*])
         :where [?e :paciente/historia-clinica 23300]]
       db)
  
 
  (d/pull db '[*] :paciente/historia-clinica)
  (d/pull db '[*] 17592186045418)
  (d/pull db '[*] 17592186045425)
  (map #(d/pull db '[*] (first %)) #{[17592186045425] [17592186045426] [17592186045423]})

  
;; Busca excepciones desde una fecha  
(d/q '[:find ?e ?evento ?origen ?excepcion ?hc
       :keys id evento origen excepcion historia-clinica
       :in $ $fecha
       :where [$fecha ?e :event/name ?evento]
              [$ ?e :event/origin ?origen]
              [$ ?e :paciente/historia-clinica ?hc]
              [$ ?e :evento/excepcion ?excepcion]]
     db
     #_(d/since db #inst "2024-10-19")
     (d/since db (clojure.instant/read-instant-date "2024-10-19")))
  
;; Busca evento por origen
  
  (d/q '[:find ?e ?evento ?hc ?hcu ?excepcion ?estado ?tx
         :keys id evento hc hcu excepcion registro fecha
         :in $ ?origen
         :where 
         [?e :event/origin ?origen ?tx]
         [?e :event/name ?evento ?tx]
         [?e :paciente/historia-clinica ?hc ?tx]
         [?e :paciente/historia-clinica-unica ?hcu ?tx]
         [?e :evento/excepcion ?excepcion ?tx]
         [?e :evento/estado ?estado ?tx]]
       db
       :evento/cirugia)
  

  (let [tipos #{:db.type/bigdec
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
    (some tipos [:db.chingada]))

  (agregar-nuevo-atributo :chupa/cabras :db.type/string :db.cardinality/one "Cualquier vaina" false)
  (agregar-nuevo-atributo :chupa/cabras :db.type/string :db.cardinality/one "Cualquier vaina" true)
  (agregar-nuevo-atributo :chupa/cabras :db.type/string :db.cardinality "Cualquier vaina" false)
  (agregar-nuevo-atributo :chupa/cabras :string :db.cardinality/one "Cualquier vaina" false)
  (agregar-nuevo-atributo 'chancho/bendito :db.type/string :db.cardinality/one "Cualquier vaina" false)
  
  )