(ns sanatoriocolegiales.logging-servoy.persistence.datomic.esquema)

(def log-schema [{:db/ident :evento/id
                  :db/cardinality :db.cardinality/one
                  :db/valueType :db.type/uuid
                  :db/unique :db.unique/identity
                  :db/doc "Identificador unico del evento"}
                 {:db/ident :evento/origen
                  :db/valueType :db.type/ref
                  :db/cardinality :db.cardinality/one
                  :db/doc "Fuente del evento"}
                 {:db/ident :evento/nombre
                  :db/valueType :db.type/string
                  :db/cardinality :db.cardinality/one
                  :db/doc "Nombre del evento"}
                 {:db/ident :paciente/historia_clinica
                  :db/valueType :db.type/long
                  :db/cardinality :db.cardinality/one
                  :db/doc "Historia clínica internacion"}
                 {:db/ident :paciente/historia_clinica_unica
                  :db/valueType :db.type/long
                  :db/cardinality :db.cardinality/one
                  :db/doc "Historia clínica única"}
                 {:db/ident :evento/estado
                  :db/valueType :db.type/ref
                  :db/isComponent true
                  :db/cardinality :db.cardinality/one
                  :db/doc "Estado de la aplicación"}
                 {:db/ident :convenios/nro_lote
                  :db/valueType :db.type/long
                  :db/cardinality :db.cardinality/one
                  :db/doc "Numero de lote"}
                 {:db/ident :convenios/contador_registros
                  :db/valueType :db.type/long
                  :db/cardinality :db.cardinality/one
                  :db/doc "Contador de registros"}
                 {:db/ident :estado/excepcion
                  :db/valueType :db.type/string
                  :db/cardinality :db.cardinality/many
                  :db/doc "Excepción"}
                 {:db/ident :estado/ok 
                  :db/valueType :db.type/string
                  :db/cardinality :db.cardinality/many
                  :db/doc "Registros que indican el estado actual de la aplicación"}
                 {:db/ident :paciente/tipo
                  :db/valueType :db.type/ref
                  :db/cardinality :db.cardinality/one
                  :db/doc "Resuelve a un tipo de referencia :paciente/internado o :paciente/ambulatorio"}
                 {:db/ident :evento/fecha
                  :db/valueType :db.type/instant
                  :db/cardinality :db.cardinality/one
                  :db/doc "Momento del evento"}
                 {:db/ident :paciente/internado}
                 {:db/ident :paciente/ambulatorio}
                 {:db/ident :origen/cirugia}
                 {:db/ident :origen/uco}
                 {:db/ident :origen/convenios}
                 {:db/ident :origen/hcdm}
                 {:db/ident :origen/uti}])

