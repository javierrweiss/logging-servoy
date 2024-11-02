(ns sanatoriocolegiales.logging-servoy.helpers.api-helpers
  (:require 
   [sanatoriocolegiales.logging-servoy.persistence.datomic.esquema :refer [log-schema]]
   [hyperfiddle.rcf :refer [tests]]
   [malli.core :as m]
   [malli.error :as me]
   [malli.experimental.time :as met]
   [malli.registry :as mr]
   [clojure.instant :refer [read-instant-date]]
   [com.brunobonacci.mulog :as µ])
  (:import java.time.LocalDateTime))

(mr/set-default-registry!
 (mr/composite-registry
  (m/default-schemas)
  (met/schemas)))

(defn peticion->registro
  [peticion]
  (try
    (-> peticion
        (update :evento/fecha read-instant-date)
        (update :evento/origen keyword)
        (update :paciente/tipo (fn [value] (keyword "paciente" value))))
    (catch Exception e (let [msj (ex-message e)]
                         (µ/log ::error-ingreso-log :mensaje msj :fecha (LocalDateTime/now))
                         (throw
                          (ex-info
                           (str "Argumento ilegal: " msj)
                           {:type :sanatoriocolegiales.logging-servoy.middleware/argumento-ilegal}))))))

(def origenes (->> log-schema
                   (map :db/ident)
                   (filter #(= (namespace %) "origen"))
                   (into [:enum])))
   
(def esquema-convenio-completo
  [:map {:closed true}
   [:evento/nombre string?]
   [:evento/origen origenes]
   [:evento/fecha [:re #"\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d+|\d{4}-\d{2}-\d{2}T\d{2}:\d{2}"]]
   [:convenios/contador_registros :int]
   [:convenios/nro_lote :int]])

(def esquema-convenio-opcional
  [:or 
   [:map [:evento/nombre string?]]
   [:map [:evento/origen origenes]]
   [:map [:evento/fecha [:re #"\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d+|\d{4}-\d{2}-\d{2}T\d{2}:\d{2}"]]]
   [:map [:convenios/contador_registros :int]]
   [:map [:convenios/nro_lote :int]]])

(def esquema-evento-completo
  [:map {:closed true}
   [:evento/nombre string?]
   [:evento/origen origenes]
   [:evento/fecha [:re #"\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d+|\d{4}-\d{2}-\d{2}T\d{2}:\d{2}"]]
   [:estado/ok {:optional true} [:sequential string?]]
   [:estado/excepcion {:optional true} [:sequential string?]]
   [:paciente/tipo {:optional true} [:enum "internado" "ambulatorio"]]
   [:paciente/historia_clinica {:optional true} int?]
   [:paciente/historia_clinica_unica {:optional true} int?]])

(def esquema-evento-opcional
  [:or 
   [:map [:evento/nombre :string]]
   [:map [:evento/origen origenes]]
   [:map [:evento/fecha [:re #"\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d+|\d{4}-\d{2}-\d{2}T\d{2}:\d{2}"]]]
   [:map [:estado/ok [:sequential string?]]]
   [:map [:estado/excepcion [:sequential string?]]]
   [:map [:paciente/tipo [:enum "internado" "ambulatorio"]]]
   [:map [:paciente/historia_clinica :int]]
   [:map [:paciente/historia_clinica_unica :int]]])


(tests

;;                    ESQUEMA CIRUGIA
;; verdadero cuando tiene todos los elementos mandatorios
 (m/validate esquema-evento-completo
             {:evento/nombre "ccas"
              :evento/origen  :origen/cirugia
              :evento/fecha "2024-10-20T12:56"
              :estado/excepcion ["ssds" "sdadsdsd"]}) := true
 ;; verdadero cuando tiene todos los elementos mandatorios, incluso si ok y excepcion están ambos presentes
 (m/validate esquema-evento-completo
             {:evento/nombre "sdsadssa"
              :evento/origen :origen/convenios
              :evento/fecha "2024-10-20T12:56"
              :estado/ok ["ssds" "sdad"]
              :estado/excepcion ["ssds" "sdadsdsd"]}) := true
 ;; verdadero cuando tiene todos los elementos mandatorios, si ok está presente
 (m/validate esquema-evento-completo
             {:evento/nombre "ccas"
              :evento/origen  :origen/hcdm
              :evento/fecha "2024-10-20T12:56"
              :estado/ok ["ssds" "sdad"]}) := true
 ;; falso cuando contiene alguna llave que no está registrada
 (m/validate esquema-evento-completo
             {:evento/nombre "ccas"
              :evento/origen :origen/uco
              :evento/fecha "2024-10-20T12:56"
              :estado/ok ["ssds" "sdad"]
              :cualquier-cosa 1}) := false
 (m/validate esquema-evento-completo
             {:evento/nombre "ccas"
              :evento/origen :origen/uti
              :evento/fecha "2024-10-20T12:56"
              :estado/ok ["ssds" "sdad"]
              :estado/excepcion ["ssds" "sdadsdsd"]
              :cualquier/cosa 'dsds}) := false
 ;; falso cuando la fecha no cumple con el formato Instant
 (m/validate esquema-evento-completo
             {:evento/nombre "ccas"
              :evento/origen :origen/cirugia
              :evento/fecha "2024-10-20 12:56"
              :estado/ok ["ssds" "sdad"]
              :estado/excepcion ["ssds" "sdadsdsd"]}) := false
 (m/validate esquema-evento-completo
             {:evento/nombre "ccas"
              :evento/origen :origen/uco
              :evento/fecha "2024-10-20"
              :estado/ok ["ssds" "sdad"]
              :estado/excepcion ["ssds" "sdadsdsd"]}) := false
 (m/validate esquema-evento-completo
             {:evento/nombre "ccas"
              :evento/origen :origen/cirugia
              :evento/fecha "2024/10/20"
              :estado/ok ["ssds" "sdad"]
              :estado/excepcion ["ssds" "sdadsdsd"]}) := false
;; verdadero, cuando se le pasa un Intanst convertido a String                                      
 (m/validate esquema-evento-completo
             {:evento/nombre "ccas"
              :evento/origen :origen/cirugia
              :evento/fecha (LocalDateTime/.toString (LocalDateTime/now))
              :estado/ok ["ssds" "sdad"]
              :estado/excepcion ["ssds" "sdadsdsd"]}) := true
 (m/validate esquema-evento-completo
             {:evento/nombre "ccas"
              :evento/origen :origen/hcdm
              :evento/fecha (LocalDateTime/.toString (LocalDateTime/now))
              :estado/ok ["ssds" "sdad"]
              :estado/excepcion ["ssds" "sdadsdsd"]
              :paciente/historia_clinica 1323232}) := true
 ;; falso si la historia-clinica-unica no es un número
 (m/validate esquema-evento-completo
             {:evento/nombre "ccas"
              :evento/origen :origen/cirugia
              :evento/fecha (LocalDateTime/.toString (LocalDateTime/now))
              :estado/ok ["ssds" "sdad"]
              :estado/excepcion ["ssds" "sdadsdsd"]
              :paciente/historia_clinica_unica "dssd212"}) := false
;; falso cuando origen no es una llave calificada y no está en el esquema de la db                                                      
 (m/validate esquema-evento-completo
             {:evento/nombre "ccas"
              :evento/origen "sdsadssa"
              :evento/fecha (LocalDateTime/.toString (LocalDateTime/now))
              :estado/ok ["ssds" "sdad"]
              :estado/excepcion ["ssds" "sdadsdsd"]
              :paciente/historia_clinica 1323232}) := false
 (m/validate esquema-evento-completo
             {:evento/nombre "ccas"
              :evento/origen :llave
              :evento/fecha (LocalDateTime/.toString (LocalDateTime/now))
              :estado/ok ["ssds" "sdad"]
              :estado/excepcion ["ssds" "sdadsdsd"]
              :paciente/historia_clinica 1323232}) := false
 (m/validate esquema-evento-completo
             {:evento/nombre "ccas"
              :evento/origen :evento/uco
              :evento/fecha (LocalDateTime/.toString (LocalDateTime/now))
              :estado/ok ["ssds" "sdad"]
              :estado/excepcion ["ssds" "sdadsdsd"]
              :paciente/historia_clinica 1323232}) := false
;; Esquema opcional debe tener al menos un par llave/valor                                                           
 (m/validate esquema-evento-opcional {:evento/nombre "ccas"}) := true
 (m/validate esquema-evento-opcional {}) := false
 (m/validate esquema-evento-opcional {:evento/hcdm "cual"}) := false
 (m/validate esquema-evento-opcional {:evento/fecha "2024-02-17T15:33"}) := true
 (m/validate esquema-evento-opcional {:estado/ok "dsdsd"}) := false
 (m/validate esquema-evento-opcional {:estado/ok ["dsdsd" "aas"]}) := true
 (m/validate esquema-evento-opcional {:paciente/tipo "cualquier cosa"}) := false
 (m/validate esquema-evento-opcional {:paciente/tipo "internado"}) := true
 (m/validate esquema-evento-opcional {:paciente/historia_clinica "internado"}) := false
 (m/validate esquema-evento-opcional {:paciente/historia_clinica 125}) := true

;;                              ESQUEMA CONVENIOS
 
;; Devuelve falso cuando mapa contiene llave no presente en spec                                                   
 (m/validate esquema-convenio-completo {:evento/nombre "ccas"
                                        :evento/origen  :a/sdsadssa
                                        :evento/fecha "2024-10-20T12:56"
                                        :estado/excepcion ["ssds" "sdadsdsd"]}) := false
;; Devuelve falso cuando falta una llave                                                   
 (m/validate esquema-convenio-completo {:evento/nombre "ccas"
                                        :evento/origen  :a/sdsadssa
                                        :evento/fecha "2024-10-20T12:56"
                                        :convenios/contador_registros 1}) := false
 (m/validate esquema-convenio-completo {:evento/nombre "ccas"
                                        :evento/origen  :a/sdsadssa
                                        :evento/fecha "2024-10-20T12:56"
                                        :convenios/nro_lote 12}) := false
;; Devuelve true cuando esquema está completo
 (m/validate esquema-convenio-completo {:evento/nombre "ccas"
                                        :evento/origen  :origen/convenios
                                        :evento/fecha "2024-10-20T12:56"
                                        :convenios/nro_lote 12
                                        :convenios/contador_registros 22}) := true
;; Acepta elementos parciales, presentes en el mapa
 (m/validate esquema-convenio-opcional {:evento/nombre "alfa"}) := true
 (m/validate esquema-convenio-opcional {:convenios/contador_registros 13}) := true
;; Devuelve falso cuando no cumple con los specs                                                                               
 (m/validate esquema-convenio-opcional {}) := false
 (m/validate esquema-convenio-opcional {:evento/chimbo "alfa"}) := false


;;                          PETICIONES A REGISTRO
 (def body-params {:evento/nombre "EVENTO X"
                   :evento/origen "origen/uco"
                   :evento/fecha "2024-02-20"
                   :paciente/historia_clinica 3212
                   :estado/ok ["wes" "ssdsd"]
                   :paciente/tipo "internado"})
 
 (def body-params2 {:evento/nombre "EVENTO X"
                   :evento/origen "origen/uti"
                   :evento/fecha "2024-02-22"
                   :paciente/historia_clinica 32121
                   :paciente/historia_clinica_unica 232121
                   :paciente/tipo "ambulatorio"
                   :estado/excepcion ["wes" "ssdsd"]})
 
 (def body-params3 {:evento/nombre "EVENTO X"
                    :evento/origen "origen/uti"
                    :evento/fecha "2024-02-40"
                    :paciente/historia_clinica 32121
                    :paciente/historia_clinica_unica 232121
                    :paciente/tipo "ambulatorio"
                    :estado/excepcion ["wes" "ssdsd"]})
 
 (peticion->registro body-params) := {:evento/nombre "EVENTO X"
                                      :evento/origen :origen/uco
                                      :evento/fecha (read-instant-date "2024-02-20")
                                      :paciente/historia_clinica 3212
                                      :estado/ok ["wes" "ssdsd"]
                                      :paciente/tipo :paciente/internado}
 
 (peticion->registro body-params2) := {:evento/nombre "EVENTO X"
                                       :evento/origen :origen/uti
                                       :evento/fecha (read-instant-date "2024-02-22")
                                       :paciente/historia_clinica 32121
                                       :paciente/historia_clinica_unica 232121
                                       :paciente/tipo :paciente/ambulatorio
                                       :estado/excepcion ["wes" "ssdsd"]}
 (peticion->registro body-params3) :throws clojure.lang.ExceptionInfo
 
 :rcf)  
 
(comment
  (-> (m/explain esquema-evento-opcional {:evento/ok ["dsdsd" "aas"]})
      (me/humanize))
  (-> (m/explain esquema-evento-opcional {:evento/nombre "ccas"})
      (me/humanize))

  (m/validate [:map [:evento/origen origenes]] {:evento/origen :origen/uco})
  
  )