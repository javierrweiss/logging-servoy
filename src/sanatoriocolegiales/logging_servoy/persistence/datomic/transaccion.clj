(ns sanatoriocolegiales.logging-servoy.persistence.datomic.transaccion
  (:require [datomic.api :as d]
            [sanatoriocolegiales.logging-servoy.persistence.datomic.esquema :refer [log-schema]]
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

(defn obtener-estado-db!
  [conn]
  (when-not conn
    (throw (IllegalArgumentException. "No existe conexión a la base de datos")))
  (try
    (d/db conn)
    (catch Exception e (µ/log ::error-al-obtener-db :mensaje (ex-message e)))))

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
  
(comment

  (d/get-database-names "datomic:sql://*?jdbc:postgresql://10.200.0.190:5432/bases_auxiliares?user=auxiliar&password=auxi2013")

  (d/delete-database (System/getenv "DATOMIC"))

  (d/create-database (System/getenv "DATOMIC"))

  (registrar-esquema! cnn)
    
  (def cnn (-> (:donut.system/instances (system-repl/system))
               :db
               :datomic))

  (ejecutar! cnn [{:db/ident :evento/estado
                   :db/isComponent true}])

  (ejecutar! cnn [{:evento/origen :evento/cirugia
                   :evento/nombre "ERROR_SELECCIONA_PACIENTE"
                   :evento/fecha (clojure.instant/read-instant-date "2024-02-19T17:34:22.711")
                   :paciente/historia-clinica 3167170
                   :paciente/historia-clinica-unica 295550
                   :paciente/tipo :paciente/internado
                   :evento/estado {:estado/excepcion ["Servoy Crazy Exception!!!"]}}
                  {:evento/origen :evento/cirugia
                   :evento/nombre "SELECCIONA_PACIENTE"
                   :evento/fecha (clojure.instant/read-instant-date "2024-02-19T17:31:22.711")
                   :paciente/historia-clinica 3167170
                   :paciente/historia-clinica-unica 295550
                   :paciente/tipo :paciente/internado
                   :evento/estado {:estado/ok ["{\"historia_clinica\":3167170,
                                                \"nombre\":\"ARANEO ANDRES SEBASTIAN\",
                                                \"fecha_ingreso\":20231220,
                                                \"hora_ingreso\":949,
                                                \"tipo_busqueda\":\"alfabética\"}"]}}
                  {:evento/origen :evento/cirugia
                   :evento/nombre "SEGURIDAD_QUIRURGICA_CIRCULANTE"
                   :evento/fecha (clojure.instant/read-instant-date "2024-02-19T17:31:22.711")
                   :paciente/historia-clinica 3167170
                   :paciente/tipo :paciente/internado}
                  {:evento/origen :evento/cirugia
                   :evento/nombre "INGRESA_EVALUACION_ANESTESICA_PREOPERATORIA"
                   :evento/fecha (clojure.instant/read-instant-date "2024-02-19T17:31:22.711")
                   :paciente/historia-clinica 3167170
                   :paciente/historia-clinica-unica 295550
                   :paciente/tipo :paciente/internado}
                  {:evento/origen :evento/cirugia
                   :evento/nombre "INGRESA_EVALUACION_ANESTESICA_PREOPERATORIA"
                   :evento/fecha (clojure.instant/read-instant-date "2024-02-19T17:31:22.711")
                   :paciente/historia-clinica 3167170
                   :paciente/historia-clinica-unica 295550
                   :paciente/tipo :paciente/internado}
                  {:evento/origen :evento/cirugia
                   :evento/nombre "GUARDA_EVALUACION_ANESTESICA_PREOPERATORIA"
                   :evento/fecha (clojure.instant/read-instant-date "2024-02-19T17:31:22.711")
                   :paciente/historia-clinica 3167170
                   :paciente/tipo :paciente/internado
                   :evento/estado {:estado/ok ["{\"registro_a_guardar\":
                                                {\"anes_histclin\":3167170,\"anes_feccarga\":20240219,\"anes_horcarga\":1749,\"anes_estado\":0, \"anes_numero\":0,            
                                                \"anes_protocolo\":0,\"anes_codlegamed\":166,\"anes_tiplegamed\":1,\"anes_interven\":1071,\"anes_tipoint\":0,
                                                \"anes_secreali\":1,\"anes_presionmax\":180,\"anes_presionmin\":100,\"anes_pulsofrec\":180,\"anes_pulsocarac\":\"NORMAL\",
                                                \"anes_asa\":2,\"anes_tipocir\":2,\"anes_escalam\":2,\"anes_horini\":1200,\"anes_filler_1\":\" \",\"anes_complipre_8\": " ",
                                                \"anes_complipre_7\":\" \",\"anes_complipre_6\":\" \",\"anes_complipre_5\":\" \",\"anes_complipre_4\":\" \",
                                                \"anes_complipre_3\":\" \",\"anes_complipre_2\":\"X\", \"anes_complipre_1\":\" \",\"anes_clinpre_33\":\" \"
                                                ,\"anes_clinpre_32\":\" \",\"anes_clinpre_31\":\" \",\"anes_clinpre_30\":\" \",\"anes_clinpre_29\":\" \",\"anes_clinpre_28\":\" \",\"anes_clinpre_27\":\" \",
                                                                         \"anes_clinpre_26\":\" \",\"anes_clinpre_25\":\" \",\"anes_clinpre_24\":\" \",
                                                                         \"anes_clinpre_23\":\" \",\"anes_clinpre_22\":\" \",\"anes_clinpre_21\":\" \",
                                                \"anes_clinpre_20\":\" \",\"anes_clinpre_19\":\" \",\"anes_clinpre_18\":\" \",\"anes_clinpre_17\":\" \",
                                                \"anes_clinpre_16\":\" \",\"anes_clinpre_15\":\" \",\"anes_clinpre_14\":\" \",\"anes_clinpre_13\":\" \",
                                                \"anes_clinpre_12\":\"X\",\"anes_clinpre_11\":\" \",\"anes_clinpre_10\":\" \",\"anes_clinpre_9\":\" \",
                                                \"anes_clinpre_8\":\" \",\"anes_clipnre_7\":\" \",\"anes_clinpre_6\":\" \",\"anes_clinpre_5\":\" \",
                                                \"anes_clinpre_4\":\" \",\"anes_clinpre_3\":\" \",\"anes_clinpre_2\":\" \",\"anes_clinpre_1\":\" \",
                                                \"anes_tratprev_19\":\" \",\"anes_tratprev_18\":\" \",\"anes_tratprev_17\":\" \",\"anes_tratprev_16\":\"X\",
                                          \"anes_tratprev_15\":\" \",\"anes_tratprev_14\":\" \",\"anes_tratprev_13\":\" \",\"anes_tratprev_12\":\" \",
                                          \"anes_tratprev_11\":\" \",\"anes_tratprev_10\":\" \",\"anes_tratprev_9\":\" \",\"anes_tratprev_8\":\" \",
                                          \"anes_tratprev_7\":\" \",\"anes_tratprev_6\":\" \",\"anes_tratprev_5\":\" \",\"anes_tratprev_4\":\" \",
                                          \"anes_tratprev_3\":\" \",\"anes_tratprev_2\":\" \",\"anes_tratprev_1\":\" \",\"anes_tratpreobs\":\" \",
                                          \"anes_cabcue_5\":\" \",\"anes_cabcue_4\":\" \",\"anes_cabcue_3\":\"X\",\"anes_cabcue_2\":\" \",
                                          \"anes_cabcue_1\":\" \",\"anes_anesregio_5\":\" \",\"anes_anesregio_4\":\" \",\"anes_anesregio_3\":\" \",
                                        \"anes_anesregio_2\":\"X\",\"anes_anesregio_1\":\" \",\"anes_anesregioobs\":\" \",\"anes_exacompl_21\":\" \",
                                        \"anes_exacompl_20\":\" \",\"anes_exacompl_19\":\" \",\"anes_exacompl_18\":\" \",\"anes_exacompl_17\":\"X\",
                                        \"anes_exacompl_16\":\" \",\"anes_sector\":0,\"anes_anesoper_9\":0,\"anes_anesoper_8\":0,\"anes_anesoper_7\":0,
                                      \"anes_anesoper_6\":0,\"anes_anesoper_5\":0,\"anes_anesoper_4\":0,\"anes_anesoper_3\":0,\"anes_anesoper_2\":0,
                                      \"anes_anesoper_1\":0,\"anes_shock_3\":0,\"anes_shock_2\":0,\"anes_shock_1\":0,\"anes_esca_5\":0,\"anes_esca_4\":0,
                                      \"anes_esca_3\":0,\"anes_esca_2\":0,\"anes_esca_1\":0,\"anes_filler_3\":\" \"}}"]}}])

  (def db (d/db cnn))
  
  (def reglas '[[(cirugia ?hc ?hcu ?nombre ?estado ?fecha)
                 [?e :paciente/historia-clinica ?hc]
                 [?e :paciente/historia-clinica-unica ?hcu]
                 [?e :evento/nombre ?nombre]
                 [?e :evento/estado ?estado]
                 [?e :evento/fecha ?fecha]]])

  (d/q '[:find ?hcu ?nombre ?estado ?fecha ?origen ?tipo
         :in $ % ?h
         :where
         (cirugia ?h ?hcu ?nombre ?estado ?fecha)
         [?e :evento/origen ?o]
         [?o :db/ident ?origen]
         [?e :paciente/tipo ?t]
         [?t :db/ident ?tipo]]
       db
       reglas
       3167170)

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
  
  
  

  (agregar-nuevo-atributo :chupa/cabras :db.type/string :db.cardinality/one "Cualquier vaina" false)
  (agregar-nuevo-atributo :chupa/cabras :db.type/string :db.cardinality/one "Cualquier vaina" true)
  (agregar-nuevo-atributo :chupa/cabras :db.type/string :db.cardinality "Cualquier vaina" false)
  (agregar-nuevo-atributo :chupa/cabras :string :db.cardinality/one "Cualquier vaina" false)
  (agregar-nuevo-atributo 'chancho/bendito :db.type/string :db.cardinality/one "Cualquier vaina" false)

  (type cnn)
  )