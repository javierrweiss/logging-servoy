(ns sanatoriocolegiales.logging-servoy.persistence.persistence-api)

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

