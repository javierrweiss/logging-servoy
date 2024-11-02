(ns sanatoriocolegiales.logging-servoy.api.logger
  "Gameboard API Scoreboard across all games"
  (:require
   [ring.util.response :refer [status created]]  
   [com.brunobonacci.mulog :as µ] 
   [sanatoriocolegiales.logging-servoy.persistence.persistence-api :as persistence-api]
   [sanatoriocolegiales.logging-servoy.helpers.api-helpers :as helpers])
  (:import java.time.LocalDateTime))

(defn crear-log
  [conexion {:keys [body-params]}]
  (let [registro (helpers/peticion->registro body-params)] 
    (try
      (persistence-api/insertar conexion [registro])
      (created "")
      (catch Exception e (let [msj (ex-message e)]
                           (µ/log ::error-ingreso-log :mensaje msj :fecha (LocalDateTime/now))
                           (throw
                            (ex-info
                             "Hubo un error al persistir los datos"
                             {:sanatoriocolegiales.logging-servoy.middleware/excepcion-persistencia msj})))))))

  (defn actualizar-log
    "Actualiza log asegurando idempotencia de la operación"
    [conexion {:keys [path-params body-params]}]
    (let [id (Long/parseLong (:id path-params))
          when-coll-first (fn [x] (if (coll? x) (first x) x))
          actualizacion (-> body-params
                            helpers/peticion->registro
                            (assoc :db/id id)
                            (update :evento/origen (fn [val] (assoc {} :db/ident val)))
                            (update :paciente/tipo (fn [val] (assoc {} :db/ident val))))
          existente (-> (try
                          (persistence-api/evento-por-id conexion id)
                          (catch Exception e (let [msj (ex-message e)]
                                               (µ/log ::error-busqueda-log-por-id :mensaje msj :id id :params path-params :fecha (LocalDateTime/now))
                                               (throw
                                                (ex-info
                                                 "Hubo un error al buscar registro por id"
                                                 {:sanatoriocolegiales.logging-servoy.middleware/excepcion-persistencia msj})))))
                        when-coll-first
                        when-coll-first)]
      (if-not (= actualizacion existente)
        (do (try
              (persistence-api/actualizar conexion [actualizacion])
              (catch Exception e (let [msj (ex-message e)]
                                   (µ/log ::error-actualizacion-log :mensaje msj :regitro actualizacion :fecha (LocalDateTime/now))
                                   (throw
                                    (ex-info
                                     "Hubo un error al intentar actualizar registro"
                                     {:sanatoriocolegiales.logging-servoy.middleware/excepcion-persistencia msj})))))
            (status 201))
        (status 204))))

(defn actualizar-log-parcialmente
  [conexion {:keys [body-params path-params]}])

(defn borrar-log
  [conexion {{:keys [id]} :path-params}]
  (try
    (persistence-api/eliminar conexion [{:db/excise (Long/parseLong id)}])
    (catch Exception e (let [msj (ex-message e)]
                         (µ/log ::error-eliminacion-log :mensaje msj :id id :fecha (LocalDateTime/now))
                         (throw
                          (ex-info
                           "Hubo un error al intentar eliminar registro"
                           {:sanatoriocolegiales.logging-servoy.middleware/excepcion-persistencia msj})))))
  (status 200))

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
            :parameters {:body helpers/esquema-evento-completo}}}]
   
   ["/cirugia/:id"
    {:swagger {:tags ["Actualizar o borrar logs de cirugía"]}
     :delete {:handler (partial borrar-log system-config)
              :parameters {:path {:id :int}}}
     :put {:handler (partial actualizar-log system-config)
           :parameters {:path {:id :int}
                        :body helpers/esquema-evento-completo}}
     :patch {:handler (partial actualizar-log-parcialmente system-config)
             :parameters {:path-params {:id :int}
                          :body helpers/esquema-evento-opcional}}}]
   
   ["/convenios"
    {:swagger {:tags ["Logging para Convenios profesionales"]}
     :post {:summary "Crea una entrada en el log"
            :handler (partial crear-log system-config)
            :parameters {:body helpers/esquema-convenio-completo}}}]
   
   ["/convenios/:id"
    {:swagger {:tags ["Actualización y eliminación de logs para Convenios profesionales"]}
     :delete {:parameters {:path {:id :int}}
              :handler (partial borrar-log system-config)}
     :put {:parameters {:path {:id :int}
                        :body helpers/esquema-convenio-completo}
           :handler (partial actualizar-log system-config)}
     :patch {:handler (partial actualizar-log-parcialmente system-config)
             :parameters {:path-params {:id :int}
                          :body helpers/esquema-convenio-opcional}}}]
   
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
  

  (persistence-api/eventos-por-historia-clinica cnn 778889)

  (persistence-api/eventos-por-historia-clinica-unica cnn 111110)

  (persistence-api/eventos-por-nombre cnn "EVEM")

  
  
  :rcf)