(ns sanatoriocolegiales.logging-servoy.persistence.esquema)

(def log-schema [{:db/ident :event/origin
                  :db/valueType :db.type/ref
                  :db/cardinality :db.cardinality/one
                  :db/doc "Fuente del evento"}
                 {:db/ident :event/name
                  :db/valueType :db.type/string
                  :db/cardinality :db.cardinality/one
                  :db/doc "Nombre del evento"}
                 {:db/ident :paciente/historia-clinica
                  :db/valueType :db.type/long
                  :db/cardinality :db.cardinality/one
                  :db/doc "Historia clínica internacion"}
                 {:db/ident :paciente/historia-clinica-unica
                  :db/valueType :db.type/long
                  :db/cardinality :db.cardinality/one
                  :db/doc "Historia clínica única"}
                 {:db/ident :evento/estado
                  :db/valueType :db.type/string
                  :db/cardinality :db.cardinality/many
                  :db/doc "Estado de la aplicación"}
                 {:db/ident :convenios/nro-lote
                  :db/valueType :db.type/long
                  :db/cardinality :db.cardinality/one
                  :db/doc "Numero de lote"}
                 {:db/ident :convenios/contador-registros
                  :db/valueType :db.type/long
                  :db/cardinality :db.cardinality/one
                  :db/doc "Contador de registros"}
                 {:db/ident :evento/excepcion
                  :db/valueType :db.type/string
                  :db/cardinality :db.cardinality/one
                  :db/doc "Excepción"}
                 {:db/ident :evento/cirugia}
                 {:db/ident :evento/uco}
                 {:db/ident :evento/convenios}
                 {:db/ident :evento/hcdm}
                 {:db/ident :evento/uti}])

