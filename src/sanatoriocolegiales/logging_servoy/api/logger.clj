(ns sanatoriocolegiales.logging-servoy.api.logger
  "API Logging para Servoy"
  (:require
   [ring.util.response :refer [status created response]]  
   [com.brunobonacci.mulog :as µ] 
   [sanatoriocolegiales.logging-servoy.persistence.persistence-api :as persistence-api]
   [sanatoriocolegiales.logging-servoy.helpers.api-helpers :as helpers]
   [clojure.instant :refer [read-instant-date]])
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
                             (str "Hubo un error al persistir los datos: " msj)
                             {:type :sanatoriocolegiales.logging-servoy.middleware/excepcion-persistencia})))))))

  (defn actualizar-log
    "Actualiza log asegurando idempotencia de la operación"
    [conexion {:keys [path-params body-params]}]
    (let [id (parse-uuid (:id path-params)) 
          actualizacion (as-> body-params bp
                          (helpers/peticion->registro bp id) 
                          (update bp :evento/origen (fn [val] (assoc {} :db/ident val)))
                          (update bp :paciente/tipo (fn [val] (assoc {} :db/ident val))))
          existente (-> (try
                          (persistence-api/evento-por-id conexion id)
                          (catch Exception e (let [msj (ex-message e)]
                                               (µ/log ::error-busqueda-log-por-id :mensaje msj :id id :params path-params :fecha (LocalDateTime/now))
                                               (throw
                                                (ex-info
                                                 (str "Hubo un error al buscar registro por id: " msj)
                                                 {:type :sanatoriocolegiales.logging-servoy.middleware/excepcion-persistencia
                                                  :mensaje msj})))))
                        flatten)]
      (if-not (= actualizacion existente)
        (do (try
              (persistence-api/actualizar conexion [actualizacion])
              (catch Exception e (let [msj (ex-message e)]
                                   (µ/log ::error-actualizacion-log :mensaje msj :regitro actualizacion :fecha (LocalDateTime/now))
                                   (throw
                                    (ex-info
                                     (str "Hubo un error al intentar actualizar registro: " msj)
                                     {:type :sanatoriocolegiales.logging-servoy.middleware/excepcion-persistencia})))))
            (status 201))
        (status 204))))

(defn actualizar-log-parcialmente
  [conexion {:keys [body-params path-params]}]
  (let [id (parse-uuid (:id path-params))
        registro (helpers/peticion->registro body-params id)]
    (try
      (persistence-api/actualizar conexion [registro])
      (status 204)
      (catch Exception e (let [msj (ex-message e)]
                           (µ/log ::error-actualizacion-log :mensaje msj :regitro registro :fecha (LocalDateTime/now))
                           (throw
                            (ex-info
                             (str "Hubo un error al intentar actualizar registro: " msj)
                             {:type :sanatoriocolegiales.logging-servoy.middleware/excepcion-persistencia})))))))

(defn borrar-log
  [conexion {{:keys [id]} :path-params}]
  (try
    (persistence-api/eliminar conexion [{:db/excise (parse-uuid id)}])
    (catch Exception e (let [msj (ex-message e)]
                         (µ/log ::error-eliminacion-log :mensaje msj :id id :fecha (LocalDateTime/now))
                         (throw
                          (ex-info
                           (str "Hubo un error al intentar eliminar registro: " msj)
                           {:type :sanatoriocolegiales.logging-servoy.middleware/excepcion-persistencia})))))
  (status 200))

(defn obtener-excepciones-por-fecha
  [conexion {{:strs [fecha]} :query-params}]
  (try
    (let [fec (read-instant-date fecha)]
      (-> (persistence-api/excepcion-desde conexion fec)
          response))
    (catch Exception e (let [msj (ex-message e)]
                         (µ/log ::error-consulta-log :mensaje msj :fecha (LocalDateTime/now))
                         (throw
                          (ex-info
                           (str "Hubo un error al intentar buscar excepciones por fecha: " msj)
                           {:type :sanatoriocolegiales.logging-servoy.middleware/excepcion-persistencia}))))))

(defn obtener-excepciones-por-origen
  [conexion {{:strs [origen]} :query-params}]
  (let [org (keyword origen)]
    (try
      (-> (persistence-api/excepcion-por-origen conexion org)
          response)
      (catch Exception e (let [msj (ex-message e)]
                           (µ/log ::error-consulta-log :mensaje msj :fecha (LocalDateTime/now))
                           (throw
                            (ex-info
                             (str "Hubo un error al intentar buscar excepciones por origen: " msj)
                             {:type :sanatoriocolegiales.logging-servoy.middleware/excepcion-persistencia})))))))

(defn obtener-eventos-por-hc
  [conexion {{:strs [hc]} :query-params}]
  (try
    (let [hc (Long/parseLong hc)]
      (-> (persistence-api/eventos-por-historia-clinica conexion hc)
          response))
    (catch Exception e (let [msj (ex-message e)]
                         (µ/log ::error-consulta-log :mensaje msj :fecha (LocalDateTime/now))
                         (throw
                          (ex-info
                           (str "Hubo un error al intentar buscar eventos por hc: " msj)
                           {:type :sanatoriocolegiales.logging-servoy.middleware/excepcion-persistencia}))))))

(defn obtener-eventos-por-hcu
  [conexion {{:strs [hcu]} :query-params}]
  (try
    (let [hcu (Long/parseLong hcu)]
      (-> (persistence-api/eventos-por-historia-clinica-unica conexion hcu)
          response))
    (catch Exception e (let [msj (ex-message e)]
                         (µ/log ::error-consulta-log :mensaje msj :fecha (LocalDateTime/now))
                         (throw
                          (ex-info
                           (str "Hubo un error al intentar buscar eventos por hcu: " msj)
                           {:type :sanatoriocolegiales.logging-servoy.middleware/excepcion-persistencia}))))))

(defn obtener-evento
  [conexion {{:strs [nombre]} :query-params}]
  (try
    (-> (persistence-api/eventos-por-nombre conexion nombre)
        response)
    (catch Exception e (let [msj (ex-message e)]
                         (µ/log ::error-consulta-log :mensaje msj :fecha (LocalDateTime/now))
                         (throw
                          (ex-info
                           (str "Hubo un error al intentar buscar evento por nombre: " msj)
                           {:type :sanatoriocolegiales.logging-servoy.middleware/excepcion-persistencia}))))))

(defn obtener-evento-id
  [conexion {{:keys [id]} :path-params}]
  (try
    (-> (persistence-api/evento-por-id conexion (parse-uuid id))
        response)
    (catch Exception e (let [msj (ex-message e)]
                         (µ/log ::error-consulta-log :mensaje msj :fecha (LocalDateTime/now))
                         (throw
                          (ex-info
                           (str "Hubo un error al intentar buscar evento por id: " msj)
                           {:type :sanatoriocolegiales.logging-servoy.middleware/excepcion-persistencia}))))))

(defn obtener-todos-los-eventos
  [conexion _]
  (try
    (-> (persistence-api/obtener-todos-los-eventos conexion)
        response)
    (catch Exception e (let [msj (ex-message e)]
                         (µ/log ::error-consulta-log :mensaje msj :fecha (LocalDateTime/now))
                         (throw
                          (ex-info
                           (str "Hubo un error al intentar recuperar todos los eventos: " msj)
                           {:type :sanatoriocolegiales.logging-servoy.middleware/excepcion-persistencia}))))))

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
              :parameters {:path {:id :uuid}}}
     :put {:handler (partial actualizar-log system-config)
           :parameters {:path {:id :uuid}
                        :body helpers/esquema-evento-completo}}
     :patch {:handler (partial actualizar-log-parcialmente system-config)
             :parameters {:path {:id :uuid}
                          :body helpers/esquema-evento-opcional}}}]
   
   ["/convenios"
    {:swagger {:tags ["Logging para Convenios profesionales"]}
     :post {:summary "Crea una entrada en el log"
            :handler (partial crear-log system-config)
            :parameters {:body helpers/esquema-convenio-completo}}}]
   
   ["/convenios/:id"
    {:swagger {:tags ["Actualización y eliminación de logs para Convenios profesionales"]}
     :delete {:parameters {:path {:id :uuid}}
              :handler (partial borrar-log system-config)}
     :put {:parameters {:path {:id :uuid}
                        :body helpers/esquema-convenio-completo}
           :handler (partial actualizar-log system-config)}
     :patch {:handler (partial actualizar-log-parcialmente system-config)
             :parameters {:path {:id :uuid}
                          :body helpers/esquema-convenio-opcional}}}]
   
   ["/excepciones_desde"
    {:swagger {:tags ["Excepciones por fecha"]}
     :get {:handler (partial obtener-excepciones-por-fecha system-config)
           :parameters {:query {:fecha [:re #"\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d+|\d{4}-\d{2}-\d{2}T\d{2}:\d{2}"]}}}}]
   
   ["/excepciones_origen"
    {:swagger {:tags ["Excepciones por origen"]}
     :get {:handler (partial obtener-excepciones-por-origen system-config)
           :parameters {:query {:origen helpers/origenes}}}}]
   
   ["/eventos_por_hc"
    {:swagger {:tags ["Eventos por Historia Clínica"]}
     :get {:handler (partial obtener-eventos-por-hc system-config)
           :parameters {:query {:hc :int}}}}]
   
   ["/eventos_por_hcu"
    {:swagger {:tags ["Eventos por Historia Clínica Unica"]}
     :get {:handler (partial obtener-eventos-por-hcu system-config)
           :parameters {:query {:hcu :int}}}}]
   
   ["/evento"
    {:swagger {:tags ["Eventos por nombre (exacto o aproximado)"]}
     :get {:handler (partial obtener-evento system-config)
           :parameters {:query {:nombre :string}}}}]
   
   ["/evento/:id"
    {:swagger {:tags ["Eventos por id"]}
     :get {:handler (partial obtener-evento-id system-config)
           :parameters {:path {:id :uuid}}}}]
   
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