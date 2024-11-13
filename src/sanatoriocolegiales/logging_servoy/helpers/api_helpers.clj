(ns sanatoriocolegiales.logging-servoy.helpers.api-helpers
  (:require
   [sanatoriocolegiales.logging-servoy.persistence.datomic.esquema :refer [log-schema]]
   [hyperfiddle.rcf :refer [tests]]
   [datomic.api :as d]
   [malli.core :as m]
   [malli.error :as me]
   [malli.experimental.time :as met]
   [malli.registry :as mr]
   [clojure.instant :refer [read-instant-date]]
   [com.brunobonacci.mulog :as µ]
   [malli.generator :as mg])
  (:import java.time.LocalDateTime))

(mr/set-default-registry!
 (mr/composite-registry
  (m/default-schemas)
  (met/schemas)))

(defn peticion->registro
  ([peticion]
   (peticion->registro peticion nil))
  ([peticion id]
   (try
     (cond-> (assoc peticion :evento/id (d/squuid))
       id (update :evento/id (constantly id))
       (get peticion :evento/fecha) (update :evento/fecha read-instant-date)
       (get peticion :evento/origen) (update :evento/origen keyword)
       (get peticion :paciente/tipo) (update :paciente/tipo (fn [value] (keyword "paciente" value))))
     (catch Exception e (let [msj (ex-message e)]
                          (µ/log ::error-ingreso-log :mensaje msj :fecha (LocalDateTime/now))
                          (throw
                           (ex-info
                            (str "Argumento ilegal: " msj)
                            {:type :sanatoriocolegiales.logging-servoy.middleware/argumento-ilegal})))))))

(def origenes (->> log-schema
                   (map :db/ident)
                   (filter #(= (namespace %) "origen"))
                   (into [:enum])))

(def esquema-convenio-completo
  [:map {:closed true}
   [:evento/nombre string?]
   [:evento/origen origenes]
   [:evento/fecha [:re #"\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d+|\d{4}-\d{2}-\d{2}T\d{2}:\d{2}"]]
   [:evento/estado 
    [:map 
     [:estado/ok {:optional true} [:sequential string?]] 
     [:estado/excepcion {:optional true} [:sequential string?]]]]
   [:convenios/contador_registros :int]
   [:convenios/nro_lote :int]])

(def esquema-convenio-opcional
  [:or
   [:map [:evento/nombre string?]]
   [:map [:evento/origen origenes]]
   [:map [:evento/fecha [:re #"\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d+|\d{4}-\d{2}-\d{2}T\d{2}:\d{2}"]]]
   [:map [:convenios/contador_registros :int]]
   [:map [:convenios/nro_lote :int]]
   [:map [:evento/estado
          [:map
           [:estado/ok {:optional true} [:sequential string?]]
           [:estado/excepcion {:optional true} [:sequential string?]]]]]])

(def esquema-evento-completo
  [:map {:closed true}
   [:evento/nombre string?]
   [:evento/origen origenes]
   [:evento/fecha [:re #"\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d+|\d{4}-\d{2}-\d{2}T\d{2}:\d{2}"]]
   [:evento/estado
    [:map
     [:estado/ok {:optional true} [:sequential string?]]
     [:estado/excepcion {:optional true} [:sequential string?]]]]
   [:paciente/tipo {:optional true} [:enum "internado" "ambulatorio"]]
   [:paciente/historia_clinica {:optional true} int?]
   [:paciente/historia_clinica_unica {:optional true} int?]])

(def esquema-evento-opcional
  [:or
   [:map [:evento/nombre :string]]
   [:map [:evento/origen origenes]]
   [:map [:evento/fecha [:re #"\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d+|\d{4}-\d{2}-\d{2}T\d{2}:\d{2}"]]] 
   [:map [:paciente/tipo [:enum "internado" "ambulatorio"]]]
   [:map [:paciente/historia_clinica :int]]
   [:map [:paciente/historia_clinica_unica :int]]
   [:map [:evento/estado
          [:map
           [:estado/ok {:optional true} [:sequential string?]]
           [:estado/excepcion {:optional true} [:sequential string?]]]]]])


(tests

;;                    ESQUEMA CIRUGIA
;; verdadero cuando tiene todos los elementos mandatorios
 (m/validate esquema-evento-completo
             {:evento/nombre "ccas"
              :evento/origen  :origen/cirugia
              :evento/fecha "2024-10-20T12:56"
              :evento/estado {:estado/excepcion ["ssds" "sdadsdsd"]}}) := true
 (m/validate esquema-evento-completo
             {:evento/nombre "sdsadssa"
              :evento/origen :origen/convenios
              :evento/fecha "2024-10-20T12:56"
              :evento/estado {:estado/ok ["ssds" "sdad"]
                              :estado/excepcion ["ssds" "sdadsdsd"]}}) := true
 (m/validate esquema-evento-completo
             {:evento/nombre "ccas"
              :evento/origen  :origen/hcdm
              :evento/fecha "2024-10-20T12:56"
              :evento/estado {:estado/ok ["ssds" "sdad"]}}) := true
 ;; falso cuando no se ajusta a spec
 (m/validate esquema-evento-completo
             {:evento/nombre "ccas"
              :evento/origen :origen/uti
              :evento/fecha "2024-10-20T12:56"
              :estado/ok ["ssds" "sdad"]
              :estado/excepcion ["ssds" "sdadsdsd"]}) := false
 ;; falso cuando contiene alguna llave que no está registrada
 (m/validate esquema-evento-completo
             {:evento/nombre "ccas"
              :evento/origen :origen/uco
              :evento/fecha "2024-10-20T12:56"
              :evento/estado {:estado/ok ["ssds" "sdad"]}
              :cualquier-cosa 1}) := false
 (m/validate esquema-evento-completo
             {:evento/nombre "ccas"
              :evento/origen :origen/uti
              :evento/fecha "2024-10-20T12:56"
              :evento/estado {:estado/ok ["ssds" "sdad"]
                              :estado/excepcion ["ssds" "sdadsdsd"]}
              :cualquier/cosa 'dsds}) := false
 ;; falso cuando la fecha no cumple con el formato Instant
 (m/validate esquema-evento-completo
             {:evento/nombre "ccas"
              :evento/origen :origen/cirugia
              :evento/fecha "2024-10-20 12:56"
              :evento/estado {:estado/ok ["ssds" "sdad"]
                              :estado/excepcion ["ssds" "sdadsdsd"]}}) := false
 (m/validate esquema-evento-completo
             {:evento/nombre "ccas"
              :evento/origen :origen/uco
              :evento/fecha "2024-10-20"
              :evento/estado {:estado/ok ["ssds" "sdad"]
                              :estado/excepcion ["ssds" "sdadsdsd"]}}) := false
 (m/validate esquema-evento-completo
             {:evento/nombre "ccas"
              :evento/origen :origen/cirugia
              :evento/fecha "2024/10/20"
              :evento/estado {:estado/ok ["ssds" "sdad"]
                              :estado/excepcion ["ssds" "sdadsdsd"]}}) := false
;; verdadero, cuando se le pasa un Intanst convertido a String                                      
 (m/validate esquema-evento-completo
             {:evento/nombre "ccas"
              :evento/origen :origen/cirugia
              :evento/fecha (LocalDateTime/.toString (LocalDateTime/now))
              :evento/estado {:estado/ok ["ssds" "sdad"]}}) := true
 (m/validate esquema-evento-completo
             {:evento/nombre "ccas"
              :evento/origen :origen/hcdm
              :evento/fecha (LocalDateTime/.toString (LocalDateTime/now))
              :evento/estado {:estado/excepcion ["ssds" "sdadsdsd"]}
              :paciente/historia_clinica 1323232}) := true
 ;; falso si la historia-clinica-unica no es un número
 (m/validate esquema-evento-completo
             {:evento/nombre "ccas"
              :evento/origen :origen/cirugia
              :evento/fecha (LocalDateTime/.toString (LocalDateTime/now))
              :evento/estado {:estado/ok ["ssds" "sdad"]}
              :paciente/historia_clinica_unica "dssd212"}) := false
;; falso cuando origen no es una llave calificada y no está en el esquema de la db                                                      
 (m/validate esquema-evento-completo
             {:evento/nombre "ccas"
              :evento/origen "sdsadssa"
              :evento/fecha (LocalDateTime/.toString (LocalDateTime/now))
              :evento/estado {:estado/ok ["ssds" "sdad"]
                              :estado/excepcion ["ssds" "sdadsdsd"]}
              :paciente/historia_clinica 1323232}) := false
 (m/validate esquema-evento-completo
             {:evento/nombre "ccas"
              :evento/origen :llave
              :evento/fecha (LocalDateTime/.toString (LocalDateTime/now))
              :evento/estado {:estado/ok ["ssds" "sdad"]}
              :paciente/historia_clinica 1323232}) := false
 (m/validate esquema-evento-completo
             {:evento/nombre "ccas"
              :evento/origen :evento/uco
              :evento/fecha (LocalDateTime/.toString (LocalDateTime/now))
              :evento/estado {:estado/ok ["ssds" "sdad"]
                              :estado/excepcion ["ssds" "sdadsdsd"]}
              :paciente/historia_clinica 1323232}) := false
;; Esquema opcional debe tener al menos un par llave/valor                                                           
 (m/validate esquema-evento-opcional {:evento/nombre "ccas"}) := true
 (m/validate esquema-evento-opcional {}) := false
 (m/validate esquema-evento-opcional {:evento/hcdm "cual"}) := false
 (m/validate esquema-evento-opcional {:evento/fecha "2024-02-17T15:33"}) := true
 (m/validate esquema-evento-opcional {:evento/estado {:estado/ok "dsdsd"}}) := false
 (m/validate esquema-evento-opcional {:estado/ok "dsdsd"}) := false
 (m/validate esquema-evento-opcional {:evento/estado {:estado/ok ["dsdsd" "aas"]}}) := true
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
                                        :evento/estado {:estado/ok ["al" "fa"]}
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
                   :evento/fecha "2024-02-20T12:33"
                   :paciente/historia_clinica 3212
                   :evento/estado {:estado/ok ["wes" "ssdsd"]}
                   :paciente/tipo "internado"})

 (def body-params2 {:evento/nombre "EVENTO X"
                    :evento/origen "origen/uti"
                    :evento/fecha "2024-02-22T13:56"
                    :paciente/historia_clinica 32121
                    :paciente/historia_clinica_unica 232121
                    :paciente/tipo "ambulatorio"
                    :evento/estado {:estado/excepcion ["wes" "ssdsd"]}})

 (def body-params3 {:evento/nombre "EVENTO X"
                    :evento/origen "origen/uti"
                    :evento/fecha "2024-02-40T14:34"
                    :paciente/historia_clinica 32121
                    :paciente/historia_clinica_unica 232121
                    :paciente/tipo "ambulatorio"
                    :evento/estado {:estado/excepcion ["wes" "ssdsd"]}})

 (peticion->registro body-params) := {:evento/id _
                                      :evento/nombre "EVENTO X"
                                      :evento/origen :origen/uco
                                      :evento/fecha (read-instant-date "2024-02-20T12:33")
                                      :paciente/historia_clinica 3212
                                      :evento/estado {:estado/ok ["wes" "ssdsd"]}
                                      :paciente/tipo :paciente/internado}

 (peticion->registro body-params2) := {:evento/id _
                                       :evento/nombre "EVENTO X"
                                       :evento/origen :origen/uti
                                       :evento/fecha (read-instant-date "2024-02-22T13:56")
                                       :paciente/historia_clinica 32121
                                       :paciente/historia_clinica_unica 232121
                                       :paciente/tipo :paciente/ambulatorio
                                       :evento/estado {:estado/excepcion ["wes" "ssdsd"]}}
 
 (peticion->registro body-params3) :throws clojure.lang.ExceptionInfo

 (peticion->registro body-params 1234) := {:evento/id 1234
                                           :evento/nombre "EVENTO X"
                                           :evento/origen :origen/uco
                                           :evento/fecha (read-instant-date "2024-02-20T12:33")
                                           :paciente/historia_clinica 3212
                                           :evento/estado {:estado/ok ["wes" "ssdsd"]}
                                           :paciente/tipo :paciente/internado}

 :rcf)

(comment
  (-> (m/explain esquema-evento-opcional {:evento/ok ["dsdsd" "aas"]})
      (me/humanize))
  (-> (m/explain esquema-evento-opcional {:evento/nombre "ccas"})
      (me/humanize))

  (m/validate [:map [:evento/origen origenes]] {:evento/origen :origen/uco})

  (m/validate [:map [:a :uuid]] {:a (java.util.UUID/randomUUID)})
 
  (let [body-params {:evento/origen "origen/uco"}
        id nil #_123]
    (cond-> body-params
      id (assoc :db/id id)
      (get body-params :evento/fecha) (update :evento/fecha read-instant-date)
      (get body-params :evento/origen) (update :evento/origen keyword)
      (get body-params :paciente/tipo) (update :paciente/tipo (fn [value] (keyword "paciente" value)))))

  (require '[malli.generator :as mg])

  (mg/generate esquema-evento-completo)

  (m/validate esquema-evento-completo {:evento/nombre "Pme568444xSILyGdDu",
                                       :evento/origen :origen/hcdm,
                                       :evento/fecha "2659-04-42T42:28",
                                       :evento/estado
                                       #:estado{:ok
                                                ["A6"
                                                 "8H3M7cRB9BrDEikf"
                                                 "asOPWc5tI12nZh6n66N07V9W5g8e"
                                                 "sPvxj9PD"
                                                 "tOdx5D75ZrH45tOvK9ZscrC1U2Q5j"
                                                 "3GxXHYHUt6Hfld7"
                                                 "vQ6R7MUpxT5XECq7GtJ9Wsk"
                                                 "53EiHQAGahxemT6Z"
                                                 "79"
                                                 "T"]},
                                       :paciente/tipo "internado",
                                       :paciente/historia_clinica -1422243})
  
  (m/validate [:time/local-date-time] (LocalDateTime/now))
  :rcf)