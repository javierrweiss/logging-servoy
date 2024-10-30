(ns sanatoriocolegiales.logging-servoy.persistence.datomic.datomic-impl
  (:require [sanatoriocolegiales.logging-servoy.persistence.persistence-api :as persistence-api]
            [sanatoriocolegiales.logging-servoy.persistence.datomic.consulta :as consulta]
            [sanatoriocolegiales.logging-servoy.persistence.datomic.transaccion :as transaccion]))

(extend-protocol persistence-api/Persistencia
  datomic.peer.Connection
  (insertar [db datos]
    (transaccion/ejecutar! db datos))
  (actualizar [db datos]
    (transaccion/ejecutar! db datos))
  (eliminar [db datos]
    (transaccion/ejecutar! db datos))
  (agregar-columna-o-atributo [campo tipo-dato cardinalidad doc unico?]
    (transaccion/agregar-nuevo-atributo campo tipo-dato cardinalidad doc unico?))
  (actualizar-columna-o-atributo [campo-anterior campo-nuevo]
    (transaccion/actualizar-atributo-esquema campo-anterior campo-nuevo))
  (excepcion-desde [db fecha]
    (let [db (transaccion/obtener-estado-db! db)]
      (consulta/buscar-excepcion-desde db fecha)))
  (excepcion-por-origen [db origen]
    (let [db (transaccion/obtener-estado-db! db)]
      (consulta/buscar-excepcion-por-origen db origen)))
  (eventos-por-historia-clinica [db hc]
    (let [db (transaccion/obtener-estado-db! db)]
      (consulta/buscar-eventos-por-historia-clinica db hc)))
  (eventos-por-historia-clinica-unica [db hcu]
    (let [db (transaccion/obtener-estado-db! db)]
      (consulta/buscar-eventos-por-historia-clinica-unica db hcu)))
  (eventos-por-nombre [db nombre]
    (let [db (transaccion/obtener-estado-db! db)]
      (consulta/buscar-eventos-por-patron-de-nombre db nombre)))
  (obtener-todos-los-eventos [db]
    (let [db (transaccion/obtener-estado-db! db)]
      (consulta/obtener-origenes-eventos db))))


(comment
  (def cnn (-> (:donut.system/instances (system-repl/system))
               :db
               :datomic))
  
  (persistence-api/obtener-todos-los-eventos cnn)
  (persistence-api/eventos-por-historia-clinica cnn 1000)
  (tap> (persistence-api/eventos-por-historia-clinica cnn 3173210))
  (tap> (persistence-api/eventos-por-historia-clinica-unica cnn 232121))
  (persistence-api/excepcion-por-origen cnn :evento/cirugia)
  (persistence-api/excepcion-desde cnn "2024-01-01")
  
  
  )