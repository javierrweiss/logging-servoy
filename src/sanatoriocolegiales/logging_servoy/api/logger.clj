(ns sanatoriocolegiales.logging-servoy.api.logger
  "Gameboard API Scoreboard across all games"
  (:require
   [ring.util.response :refer [response]]
   [sanatoriocolegiales.logging-servoy.persistence.persistence-api :as persistence-api]))

(defn crear-log
  [conexion request])

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
  [["/log"
     {:swagger {:tags ["Logging para Servoy"]}
      :post {:summary "Crea una entrada en el log" 
             :handler (partial crear-log system-config)}
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

