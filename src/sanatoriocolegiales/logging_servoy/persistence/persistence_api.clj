(ns sanatoriocolegiales.logging-servoy.persistence.persistence-api
  (:require
   [sanatoriocolegiales.logging-servoy.persistence.datomic.consulta :as consulta]
   [sanatoriocolegiales.logging-servoy.persistence.datomic.transaccion :as transaccion]))

(defprotocol Persistencia
  (insertar [db datos])
  (actualizar [db datos])
  (eliminar [db datos])
  (agregar-columna-o-atributo [tabla columna tipo-dato] [this campo tipo-dato cardinalidad doc unico?])
  (actualizar-columna-o-atributo [campo-anterior campo-nuevo] [this tabla columna tipo-dato])
  (excepcion-desde [db fecha] "Obtiene las excepciones registradas a partir de la fecha dada como argumento")
  (excepcion-por-origen [db origen] "Obtiene excepciones por sector (e.g. cirugia, uti, uco, etc.)")
  (eventos-por-historia-clinica [db hc])
  (eventos-por-historia-clinica-unica [db hcu])
  (eventos-por-nombre [db nombre] "Obtiene los eventos con nombres iguales o similares al argumento")
  (obtener-todos-los-eventos [db] "Obtiene los eventos registrados en la base"))

(extend-protocol Persistencia
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