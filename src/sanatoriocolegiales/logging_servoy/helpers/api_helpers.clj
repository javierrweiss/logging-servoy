(ns sanatoriocolegiales.logging-servoy.helpers.api-helpers
  (:require [clojure.string :as string]
            [clojure.instant :refer [read-instant-date]]
            [clojure.core.match :refer [match]]
            [sanatoriocolegiales.logging-servoy.persistence.datomic.esquema :refer [log-schema]]))

(def llaves (mapv :db/ident log-schema))
 
(defn origen-str->origen-kywd
  [org]
  (match [(string/lower-case org)]
    ["uco"] :evento/uco
    ["uti"] :evento/uti
    ["convenios"] :evento/convenios
    ["hcdm"] :evento/hcdm
    ["cirugia"] :evento/cirugia))

(defn crear-registro
  [body-params uri]
  (let [base-map {:evento/nombre (:nombre body-params)
                  :evento/origen {:db/ident (origen-str->origen-kywd (:origen body-params))}
                  :evento/fecha (read-instant-date (:fecha body-params))}]
    (condp re-seq uri
      #"\/api\/v1\/cirugia" (cond-> base-map
                              (:historia_clinica body-params) (assoc :paciente/historia-clinica (:historia_clinica body-params))
                              (:historia_clinica_unica body-params) (assoc :paciente/historia-clinica-unica (:historia_clinica_unica body-params))
                              (:tipo body-params) (assoc :paciente/tipo {:db/ident (-> (str "paciente/" (:tipo body-params)) keyword)})
                              (:ok body-params) (assoc :evento/estado {:estado/ok (:ok body-params)})
                              (:excepcion body-params) (assoc :evento/estado {:estado/excepcion (:excepcion body-params)}))
      #"\/api\/v1\/convenios" (assoc base-map :convenios/nro-lote (:nro_lote body-params) :convenios/contador-registros (:contador_registros body-params))
      (throw (ex-info "Ruta no encontrada" {:parametros body-params
                                            :uri uri})))))