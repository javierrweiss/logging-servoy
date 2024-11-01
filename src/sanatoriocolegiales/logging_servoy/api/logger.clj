(ns sanatoriocolegiales.logging-servoy.api.logger
  "Gameboard API Scoreboard across all games"
  (:require
   [ring.util.response :refer [response created]] 
   [clojure.spec.alpha :as s]
   [sanatoriocolegiales.especificaciones.evento :as evento] 
   [sanatoriocolegiales.especificaciones.convenios :as convenios]
   [sanatoriocolegiales.especificaciones.paciente :as paciente]
   [sanatoriocolegiales.especificaciones.estado :as estado]
   [clojure.core.match :refer [match]]
   [clojure.instant :refer [read-instant-date]]
   [com.brunobonacci.mulog :as µ]
   [clojure.string :as string]
   [sanatoriocolegiales.logging-servoy.persistence.persistence-api :as persistence-api]
   [hyperfiddle.rcf :refer [tests]])
  (:import java.time.LocalDateTime))

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
                  :evento/origen (origen-str->origen-kywd (:origen body-params))
                  :evento/fecha (read-instant-date (:fecha body-params))}]
    (match [uri]
      ["/api/v1/cirugia"] (cond-> base-map
                            (:historia_clinica body-params) (assoc :paciente/historia-clinica (:historia_clinica body-params))
                            (:historia_clinica_unica body-params) (assoc :paciente/historia-clinica-unica (:historia_clinica_unica body-params))
                            (:tipo body-params) (assoc :paciente/tipo (-> (str "paciente/" (:tipo body-params)) keyword))
                            (:ok body-params) (assoc :evento/estado {:estado/ok (:ok body-params)})
                            (:excepcion body-params) (assoc :evento/estado {:estado/excepcion (:excepcion body-params)}))
      ["/api/v1/convenios"] (assoc base-map :convenios/nro-lote (:nro_lote body-params) :convenios/contador-registros (:contador_registros body-params)))))

(defn crear-log
  [conexion {:keys [body-params uri]}]
  (let [registro (crear-registro body-params uri)]
    (try
      (persistence-api/insertar conexion [registro])
      (created "")
      (catch Exception e (let [msj (ex-message e)]
                           (µ/log ::error-ingreso-log :mensaje msj :fecha (LocalDateTime/now) :uri uri)
                           (throw
                            (ex-info
                             "Hubo un error al persistir los datos"
                             {:sanatoriocolegiales.logging-servoy.middleware/excepcion-persistencia msj})))))))

(defn actualizar-log
  [conexion request])

(defn borrar-log
  [conexion request])

(defn obtener-excepciones-por-fecha
  [conexion request])

(defn obtener-excepciones-por-origen
  [conexion request])

(defn obtener-eventos-por-hc
  [conexion request])

(defn obtener-eventos-por-hcu
  [conexion request])

(defn obtener-evento
  [conexion request])

(defn obtener-todos-los-eventos
  [conexion request])

(defn routes
  [system-config]
  [["/cirugia"
    {:swagger {:tags ["Logging para protocolos ambulatorio e internado, UTI, UCO e Historia Clínica Digital Ambulatoria"]}
     :post {:summary "Crea una entrada en el log"
            :handler (partial crear-log system-config)
            :parameters {:body (s/keys :req-un [(and ::evento/nombre
                                                     ::evento/origen
                                                     ::evento/fecha
                                                     (and (or ::estado/ok ::estado/excepcion)
                                                          (or (and ::paciente/tipo ::paciente/historia_clinica)
                                                              (and ::paciente/tipo ::paciente/historia_clinica_unica)
                                                              (and ::paciente/historia_clinica
                                                                   ::paciente/historia_clinica_unica
                                                                   ::paciente/tipo))))])}}}]
   ["/cirugia/:id"
    {:swagger {:tags ["Actualizar o borrar logs"]}
     :delete {:handler (partial borrar-log system-config)
              :parameters {:path {:id ::evento/id}}}
     :put {:handler (partial actualizar-log system-config)
           :parameters {:path {:id ::evento/id}
                        :body (s/keys :req-un [(and ::evento/nombre
                                                    ::evento/origen
                                                    ::evento/fecha
                                                    (and (or ::estado/ok ::estado/excepcion)
                                                         (or (and ::paciente/tipo ::paciente/historia_clinica)
                                                             (and ::paciente/tipo ::paciente/historia_clinica_unica)
                                                             (and ::paciente/historia_clinica
                                                                  ::paciente/historia_clinica_unica
                                                                  ::paciente/tipo))))])}}}]
   ["/convenios"
    {:swagger {:tags ["Logging para Convenios profesionales"]}
     :post {:summary "Crea una entrada en el log"
            :handler (partial crear-log system-config)
            :parameters {:body (s/keys :req-un [::evento/nombre
                                                ::evento/origen
                                                ::evento/fecha
                                                ::convenios/contador_registros
                                                ::convenios/nro_lote])}}
     :delete {:handler (partial borrar-log system-config)}
     :put {:handler (partial actualizar-log system-config)}}]
   ["/excepciones_desde"
    {:swagger {:tags ["Excepciones por fecha"]}
     :get {:handler (partial obtener-excepciones-por-fecha system-config)}}]
   ["/excepciones_origen"
    {:swagger {:tags ["Excepciones por origen"]}
     :get {:handler (partial obtener-excepciones-por-origen system-config)}}]
   ["/eventos_por_hc"
    {:swagger {:tags ["Eventos por Historia Clínica"]}
     :get {:handler (partial obtener-eventos-por-hc system-config)}}]
   ["/eventos_por_hcu"
    {:swagger {:tags ["Eventos por Historia Clínica Unica"]}
     :get {:handler (partial obtener-eventos-por-hcu system-config)}}]
   ["/evento"
    {:swagger {:tags ["Eventos por nombre"]}
     :get {:handler (partial obtener-evento system-config)}}]
   ["/todos_eventos"
    {:swagger {:tags ["Todos los eventos"]}
     :get {:handler (partial obtener-todos-los-eventos system-config)}}]])

(comment

  (def cnn (-> (:donut.system/instances (system-repl/system))
               :db
               :datomic))

  (let [body-params {:nombre "EVENTO X"
                     :origen "UTI"
                     :fecha "2024-02-20"
                     :historia_clinica 3212
                     :ok ["wes" "ssdsd"]
                     :tipo "internado"}
        body-params2 {:nombre "EVENTO X"
                      :origen "UTI"
                      :fecha "2024-02-22"
                      :historia_clinica 32121
                      :historia_clinica_unica 232121
                      :tipo "ambulatorio"
                      :excepcion ["wes" "ssdsd"]}]
    #_(cond-> {:evento/nombre (:nombre body-params)
               :evento/origen (:origen body-params)
               :evento/fecha (:fecha body-params)}
        (:historia-clinica body-params) (assoc :paciente/historia-clinica (:historia-clinica body-params))
        (:historia-clinica-unica body-params) (assoc :paciente/historia-clinica-unica (:historia-clinica-unica body-params))
        (:tipo body-params) (assoc :paciente/tipo (-> (str "paciente/" (:tipo body-params)) keyword))
        (:ok body-params) (assoc :evento/estado {:estado/ok (:ok body-params)})
        (:excepcion body-params) (assoc :evento/estado {:estado/excepcion (:excepcion body-params)}))
    #_(persistence-api/insertar cnn [(crear-registro body-params "/api/v1/cirugia")])
    #_(crear-registro body-params "/api/v1/cirugia")
    (crear-registro body-params2 "/api/v1/cirugia")
    #_(crear-log cnn {:body-params body-params2
                    :uri "/api/v1/cirugia"}))
  
  (persistence-api/eventos-por-historia-clinica cnn 778889)

  (persistence-api/eventos-por-historia-clinica-unica cnn 111110)
  
  (persistence-api/eventos-por-nombre cnn "EVEM")



  )