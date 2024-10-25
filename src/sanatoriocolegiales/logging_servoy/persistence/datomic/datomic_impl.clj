(ns sanatoriocolegiales.logging-servoy.persistence.datomic.datomic-impl
  (:require [sanatoriocolegiales.logging-servoy.persistence.persistence-api :as persistence-api]
            [sanatoriocolegiales.logging-servoy.persistence.datomic.consulta :as consulta]
            [sanatoriocolegiales.logging-servoy.persistence.datomic.transaccion :as transaccion]))

(extend-protocol persistence-api/Persistencia
  datomic.peer.Connection
  (insertar [this db datos]
    (transaccion/ejecutar! db datos))
  (actualizar [this db datos]
    (transaccion/ejecutar! db datos))
  (eliminar [this db datos]
    (transaccion/ejecutar! db datos))
  (agregar-columna-o-atributo [this campo tipo-dato cardinalidad doc unico?]
    (transaccion/agregar-nuevo-atributo campo tipo-dato cardinalidad doc unico?))
  (actualizar-columna-o-atributo [this campo-anterior campo-nuevo]
    (transaccion/actualizar-atributo-esquema campo-anterior campo-nuevo))
  (excepcion-desde [this db fecha]
    (let [db (transaccion/obtener-estado-db! db)]
      (consulta/buscar-excepcion-desde db fecha)))
  (excepcion-por-origen [this db origen]
    (let [db (transaccion/obtener-estado-db! db)]
      (consulta/buscar-excepcion-por-origen db origen)))
  (eventos-por-historia-clinica [this db hc]
    (let [db (transaccion/obtener-estado-db! db)]
      (consulta/buscar-eventos-por-historia-clinica db hc)))
  (eventos-por-historia-clinica-unica [this db hcu]
    (let [db (transaccion/obtener-estado-db! db)]
      (consulta/buscar-eventos-por-historia-clinica-unica db hcu)))
  (eventos-por-nombre [this db nombre]
    (let [db (transaccion/obtener-estado-db! db)]
      (consulta/buscar-eventos-por-patron-de-nombre db nombre)))
  (obtener-todos-los-eventos [this db]
    (let [db (transaccion/obtener-estado-db! db)]
      (consulta/obtener-origenes-eventos db))))