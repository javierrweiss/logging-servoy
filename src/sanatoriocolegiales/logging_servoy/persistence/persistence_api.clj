(ns sanatoriocolegiales.logging-servoy.persistence.persistence-api)

(defprotocol Persistencia
  (insertar [this db datos])
  (actualizar [this db datos])
  (eliminar [this db datos])
  (agregar-columna-o-atributo [this tabla columna tipo-dato] [this campo tipo-dato cardinalidad doc unico?])
  (actualizar-columna-o-atributo [this campo-anterior campo-nuevo] [this tabla columna tipo-dato])
  (excepcion-desde [this db fecha] "Obtiene las excepciones registradas a partir de la fecha dada como argumento")
  (excepcion-por-origen [this db origen] "Obtiene excepciones por sector (e.g. cirugia, uti, uco, etc.)")
  (eventos-por-historia-clinica [this db hc])
  (eventos-por-historia-clinica-unica [this db hcu])
  (eventos-por-nombre [this db nombre] "Obtiene los eventos con nombres iguales o similares al argumento")
  (obtener-todos-los-eventos [this db] "Obtiene los eventos registrados en la base"))

