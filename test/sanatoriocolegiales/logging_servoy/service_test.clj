(ns sanatoriocolegiales.logging-servoy.service-test
  (:require [clojure.test :refer [deftest is testing use-fixtures run-test]] 
            [org.httpkit.client :as client]
            [cheshire.core :as json]
            [donut.system :as donut]
            [datomic.api :as d]
            [sanatoriocolegiales.logging-servoy.persistence.datomic.esquema :refer [log-schema]]
            [sanatoriocolegiales.logging-servoy.system :refer [main]]
            [com.brunobonacci.mulog :as µ]
            [clojure.instant :refer [read-instant-date]])
  (:import java.io.IOException))

(def transaccion-test [{:evento/id #uuid "67324c94-4e3a-4526-a10c-1c8a418bc280"
                        :evento/nombre "Evento de prueba por defecto"
                        :evento/fecha (clojure.instant/read-instant-date "2024-01-01T10:00:00Z")
                        :evento/origen :origen/uti
                        :evento/estado {:estado/ok ["Bien" "Vamos!!"]}
                        :paciente/tipo :paciente/ambulatorio
                        :paciente/historia_clinica 33232
                        :paciente/historia_clinica_unica 774455}
                       {:evento/id (d/squuid)
                        :evento/nombre "Evento de pruebah"
                        :evento/fecha (read-instant-date "2024-01-01T00:00:00Z")
                        :evento/origen :origen/uti
                        :evento/estado {:estado/ok ["Bien" "Vamos!!"]}
                        :paciente/tipo :paciente/internado
                        :paciente/historia_clinica 133232
                        :paciente/historia_clinica_unica 774455}
                       {:evento/id #uuid "67325217-4fe1-447b-bfa1-f007d9dce44b"
                        :evento/nombre "Evento para borrar"
                        :evento/fecha (read-instant-date "2024-02-01T00:00:00Z")
                        :evento/origen :origen/uco
                        :evento/estado {:estado/excepcion ["Rayos!" "Nooo!!"]}
                        :paciente/tipo :paciente/internado
                        :paciente/historia_clinica 133232
                        :paciente/historia_clinica_unica 7754455}
                       {:evento/id (d/squuid)
                        :evento/nombre "CONVENIO X"
                        :evento/fecha (read-instant-date "2024-10-11T00:00:00Z")
                        :evento/origen :origen/convenios
                        :evento/estado {:estado/excepcion ["Rayos!" "Nooo!!"]}
                        :convenios/contador_registros 12
                        :convenios/nro_lote 21}
                       {:evento/id #uuid "67325344-ee80-4bde-aa58-dc2a49188772"
                        :evento/nombre "CONVENIO X para borrar"
                        :evento/fecha (read-instant-date "2024-10-12T00:00:00Z")
                        :evento/origen :origen/convenios
                        :evento/estado {:estado/excepcion ["Rayosss!" "Nooo!!"]}
                        :convenios/contador_registros 122
                        :convenios/nro_lote 21}])

(defmethod donut/named-system :test
  [_]
  (donut/system main {[:env :app-env] "test"
                      [:env :http-port] 2500 
                      [:env :persistence :datomic-conn-string] "datomic:mem://logging"
                      [:db :datomic ::donut/start] (fn conexion-datomic-test
                                                     [{{:keys [conn-str]} ::donut/config}]
                                                     (try
                                                       (µ/log ::estableciendo-conexion-datomic-test)
                                                       (d/create-database conn-str)
                                                       (let [conn (d/connect conn-str)]
                                                         (µ/log ::obteniendo-conexion-datomic-test :conexion conn)
                                                         @(d/transact conn log-schema)
                                                         @(d/transact conn transaccion-test)
                                                         conn)
                                                       (catch IOException e (µ/log ::error-conexion-datomic :mensaje (ex-message e)))))}))

(use-fixtures :once (fn [f]
                      (let [sys (donut/start :test)]
                        (f)
                        (d/delete-database "datomic:mem://logging")
                        (donut/stop sys))))

(def url "http://localhost:2500/api/v1")

(deftest service-test
  (testing "CIRUGIA"

    (testing "Cuando recibe una solicitud con el método POST al endpoint /cirugia devuelve 201 cuando el recurso fue creado"
      (let [respuesta (-> @(client/post (str url "/cirugia")
                                        {:headers {"Content-Type" "application/json"}
                                         :body  (json/encode {:evento/nombre "EVENTO DE PRUEBA 1"
                                                              :evento/origen "origen/uti"
                                                              :evento/fecha "2024-12-01T12:53"
                                                              :evento/estado {:estado/ok ["Todo está bien"]}
                                                              :paciente/tipo "internado"
                                                              :paciente/historia_clinica 101023
                                                              :paciente/historia_clinica_unica 155446})})
                          :status)]
        (is (== 201 respuesta))))

    (testing "Cuando recibe una solicitud con el método POST al endpoint /cirugia devuelve 400 cuando el body no tiene la forma correcta"
      (let [respuesta (-> @(client/post (str url "/cirugia")
                                        {:headers {"Content-Type" "application/json"}
                                         :body  (json/encode {:evento/nombre "EVENTO DE PRUEBA 1"
                                                              :evento/origen "origen/uti"
                                                              :evento/fecha "2024-12-01T12:53"
                                                              :evento/estado "Excepcion"
                                                              :paciente/tipo "internado"
                                                              :paciente/historia_clinica 101023
                                                              :paciente/historia_clinica_unica 155446})})
                          :status)]
        (is (== 400 respuesta))))

    (testing "Cuando recibe una solicitud con el método PUT al endpoint /cirugia devuelve 400 cuando la petición es inválida"
      (let [respuesta (-> @(client/put (str url "/cirugia")) 
                          {:path-params {:id #uuid "67324c94-4e3a-4526-a10c-1c8a418bc280"}
                           :headers {"Content-Type" "application/json"}
                           :body  (json/encode {:evento/nombre "EVENTO ACTUALIZADO"
                                                :evento/origen "origen/uti" 
                                                :paciente/tipo "internado"
                                                :paciente/historia_clinica 101023
                                                :paciente/historia_clinica_unica 155446})}
                          :status)]
        (is (== 400 respuesta))))

    (testing "Cuando recibe una solicitud con el método PUT al endpoint /cirugia devuelve 201 cuando el recurso fue actualizado"
      (let [solicitud (fn [] @(client/put (str url "/cirugia") 
                                          {:headers {"Content-Type" "application/json"}
                                           :body  (json/encode {:evento/nombre "EVENTO ACTUALIZADO EXITOSAMENTE"
                                                                :evento/origen "origen/uti"
                                                                :evento/fecha "2024-12-11T12:53"
                                                                :evento/estado {:estado/ok ["Todo está bien"]}
                                                                :paciente/tipo "ambulatorio"
                                                                :paciente/historia_clinica 111111
                                                                :paciente/historia_clinica_unica 22222})
                                           :path-params {:id #uuid "67324c94-4e3a-4526-a10c-1c8a418bc280"}}))]
        (is (== 201 (:status (solicitud))))
        (testing "Cuando recibe una solicitud con el método PUT al endpoint /cirugia devuelve 204 cuando el recurso fue actualizado"
          (is (== 204 (:status (solicitud)))))))

    (testing "Cuando recibe una solicitud con el método PATCH al endpoint /cirugia devuelve 204 cuando el recurso fue actualizado"
      (let [respuesta (-> @(client/patch (str url "/cirugia") {:body (json/encode {:paciente/historia_clinica 121212})
                                                               :path-params {:id #uuid "67324c94-4e3a-4526-a10c-1c8a418bc280"}}) :status)]
        (is (== 204 respuesta))))

    (testing "Cuando recibe una solicitud con el método PATCH al endpoint /cirugia devuelve 400 cuando la petición es inválida"
      (let [respuesta (-> @(client/patch (str url "/cirugia") {:body (json/encode {:paciente/no-existente 111222})
                                                               :path-params {:id #uuid "67324c94-4e3a-4526-a10c-1c8a418bc280"}}) :status)]
        (is (== 400 respuesta))))

    (testing "Cuando recibe una solicitud con el método DELETE al endpoint /cirugia devuelve 200 cuando fue exitoso"
      (let [respuesta (-> @(client/delete (str url "/cirugia") {:path-params {:id #uuid "67325217-4fe1-447b-bfa1-f007d9dce44b"}}) :status)]
        (is (== 200 respuesta))))

    (testing "Cuando recibe una solicitud con el método DELETE al endpoint /cirugia devuelve 404 cuando el recurso no existe"
      (let [respuesta (-> @(client/delete (str url "/cirugia" {:path-params {:id #uuid "261d40ef-108d-4dde-99f2-f48f37ccea73"}})) :status)]
        (is (== 404 respuesta)))))

  (testing "CONVENIOS"

    (testing "Cuando recibe una solicitud con el método POST al endpoint /convenios devuelve 201 cuando el recurso fue creado"
      (let [respuesta (-> @(client/post (str url "/convenios")
                                        {:headers {"Content-Type" "application/json"}
                                         :body (json/encode {:evento/nombre "CONVENIO X"
                                                             :evento/fecha "2024-11-11T01:10:20Z"
                                                             :evento/origen :origen/convenios
                                                             :evento/estado {:estado/excepcion ["Crap!" "Oh nooo!!"]}
                                                             :convenios/contador_registros 120
                                                             :convenios/nro_lote 421})}) 
                          :status)]
        (is (== 201 respuesta))))

    (testing "Cuando recibe una solicitud con el método POST al endpoint /convenios devuelve 400 cuando el body no tiene la forma correcta"
      (let [respuesta (-> @(client/post (str url "/convenios")
                                        {:headers {"Content-Type" "application/json"}
                                         :body (json/encode {:evento/nombre "CONVENIO X"
                                                             :evento/fecha "2024-11-11T01:10:20Z"
                                                             :evento/origen :origen/convenio
                                                             :evento/estado {:estado/excepcion ["Crap!" "Oh nooo!!"]}
                                                             :convenios/contador_registros 120
                                                             :convenios/nro_lote 421})}) 
                          :status)]
        (is (== 400 respuesta))))

    (testing "Cuando recibe una solicitud con el método DELETE al endpoint /convenios devuelve 200 cuando el recurso fue borrado"
      (let [respuesta (-> @(client/delete (str url "/convenios") {:path-params {:id #uuid "67325344-ee80-4bde-aa58-dc2a49188772"}}) :status)]
        (is (== 200 respuesta))))

    (testing "Cuando recibe una solicitud con el método DELETE al endpoint /convenios devuelve 404 cuando el recurso no se encontró"
      (let [respuesta (-> @(client/delete (str url "/convenios" {:path-params {:id #uuid "261d40ef-108d-4dde-99f2-f48f37ccea73"}})) :status)]
        (is (== 404 respuesta))))

    (testing "Cuando recibe una solicitud con el método PUT al endpoint /convenios devuelve 201 cuando el recurso fue actualizado"
      (let [solicitud (fn [] @(client/put (str url "/convenios") {:body  (json/encode {:evento/nombre "CONVENIO X"
                                                                                       :evento/fecha "2024-08-11T01:10:20Z"
                                                                                       :evento/origen :origen/convenios
                                                                                       :evento/estado {:estado/ok ["Bien" "Sí"]}
                                                                                       :convenios/contador_registros 1220
                                                                                       :convenios/nro_lote 4221})
                                                                  :path-params {:id #uuid "67325217-4fe1-447b-bfa1-f007d9dce44b"}}))]
        (is (== 201 (:status (solicitud))))
        (testing "Cuando recibe una solicitud con el método PUT al endpoint /convenios devuelve 204 cuando ya se recibió la misma petición de actualización"
          (is (== 204 (:status (solicitud)))))))
    
    (testing "Cuando recibe una solicitud con el método PATCH al endpoint /convenios devuelve 204 cuando el recurso fue actualizado"
      (let [respuesta (-> @(client/patch (str url "/convenios") 
                                         {:body (json/encode {:convenios/contador_registros 1220})
                                          :path-params {:id #uuid "67325217-4fe1-447b-bfa1-f007d9dce44b"}}) 
                          :status)]
        (is (== 204 respuesta)))))

  (testing "EXCEPCIONES DESDE CIERTA FECHA"

    (testing "Cuando recibe una solicitud correcta con el método GET al endpoint /excepciones_desde devuelve 200"
      (let [respuesta (-> @(client/get (str url "/excepciones_desde") {:query-params {"fecha" "2024-01-01T00:00"}}) :status)]
        (is (== 200 respuesta))))

    (testing "Cuando recibe una solicitud incorrecta con el método GET al endpoint /excepciones_desde devuelve 400"
      (let [respuesta (-> @(client/get (str url "/excepciones_desde") {:query-params {"fecha" "2024-01-01"}}) :status)]
        (is (== 400 respuesta)))))

  (testing "EXCEPCIONES POR ORIGEN"

    (testing "Cuando recibe una solicitud correcta con el método GET al endpoint /excepciones_origen devuelve 200"
      (let [respuesta (-> @(client/get (str url "/excepciones_origen") {:query-params {"origen" "origen/uco"}}) :status)]
        (is (== 200 respuesta))))

    (testing "Cuando recibe una solicitud incorrecta con el método GET al endpoint /excepciones_origen devuelve 400"
      (let [respuesta (-> @(client/get (str url "/excepciones_origen") {:query-params {"origen" "origen/ninguno"}}) :status)]
        (is (== 400 respuesta)))))

  (testing "EVENTOS POR HISTORIA CLINICA"

    (testing "Cuando recibe una solicitud correcta con el método GET al endpoint /eventos_por_hc devuelve 200"
      (let [respuesta (-> @(client/get (str url "/eventos_por_hc") {:query-params {"hc" "133232"}}) :status)]
        (is (== 200 respuesta))))

    (testing "Cuando recibe una solicitud incorrecta con el método GET al endpoint /eventos_por_hc devuelve 400"
      (let [respuesta (-> @(client/get (str url "/eventos_por_hc") {:query-params {"hc" "acd"}}) :status)]
        (is (== 400 respuesta)))))

  (testing "EVENTOS POR HISTORIA CLINICA UNICA"

    (testing "Cuando recibe una solicitud correcta con el método GET al endpoint /eventos_por_hcu devuelve 200"
      (let [respuesta (-> @(client/get (str url "/eventos_por_hcu") {:query-params {"hcu" "7754455"}}) :status)]
        (is (== 200 respuesta))))

    (testing "Cuando recibe una solicitud incorrecta con el método GET al endpoint /eventos_por_hcu devuelve 400"
      (let [respuesta (-> @(client/get (str url "/eventos_por_hcu") {:query-params {"hcu" "2ed23"}}) :status)]
        (is (== 400 respuesta)))))

  (testing "EVENTO"

    (testing "Cuando recibe una solicitud correcta con el método GET al endpoint /evento devuelve 200"
      (let [respuesta (-> @(client/get (str url "/evento") {:query-params {"nombre" "CONVENIO"}}) :status)]
        (is (== 200 respuesta))))

    (testing "Cuando recibe una solicitud incorrecta con el método GET al endpoint /evento devuelve 400"
      (let [respuesta (-> @(client/get (str url "/evento") {:query-params {"nombre" "4555"}}) :status)]
        (is (== 400 respuesta)))))

  (testing "TODOS LOS EVENTOS"

    (testing "Cuando recibe una solicitud correcta con el método GET al endpoint /todos_eventos devuelve 200"
      (let [respuesta (-> @(client/get (str url "/todos_eventos")) :status)]
        (is (= 200 respuesta))))))


(comment

  (run-test service-test)

  (clojure.test/run-all-tests)
  (def prod-url "http://localhost:3000/api/v1")
  @(client/get (str prod-url "/todos_eventos"))

  (-> @(client/post (str prod-url "/cirugia")  {:headers {"Content-Type" "application/json"}
                                                :body  (json/encode {:evento/nombre "EVENTO DE PRUEBA 1"
                                                                     :evento/origen "origen/uti"
                                                                     :evento/fecha "2024-12-01T12:53"
                                                                     :evento/estado {:estado/ok ["Todo está bien"]}
                                                                     :paciente/tipo "internado"
                                                                     :paciente/historia_clinica 12456
                                                                     :paciente/historia_clinica_unica 5546})})
      :body
      json/decode)
  
  (ns-unmap *ns* 'conn)

  (d/delete-database "datomic:mem://logging")
  (d/create-database "datomic:mem://logging")
  (defonce conn (d/connect "datomic:mem://logging"))
 
  @(d/transact conn log-schema)
  @(d/transact conn [{:evento/id #uuid "67324c94-4e3a-4526-a10c-1c8a418bc280"
                      :evento/nombre "EVENTO ACTUALIZADO EXITOSAMENTE"
                      :evento/origen :origen/uti
                      :evento/fecha (read-instant-date "2024-12-11T12:53")
                      :evento/estado {:estado/ok ["Todo está bien"]}
                      :paciente/tipo :paciente/ambulatorio
                      :paciente/historia_clinica 111111
                      :paciente/historia_clinica_unica 22222}])
  
  (sanatoriocolegiales.logging-servoy.persistence.persistence-api/evento-por-id conn #uuid "67324c94-4e3a-4526-a10c-1c8a418bc280")
 
  (tap> @(client/put (str prod-url "/cirugia")
                     {:headers {"Content-Type" "application/json"}
                      :body  (json/encode {:evento/nombre "EVENTO ACTUALIZADO EXITOSAMENTE"
                                           :evento/origen "origen/uti"
                                           :evento/fecha "2024-12-11T12:53"
                                           :evento/estado {:estado/ok ["Todo está bien"]}
                                           :paciente/tipo "ambulatorio"
                                           :paciente/historia_clinica 111111
                                           :paciente/historia_clinica_unica 22222})
                      :path-params {:id #uuid "67324c94-4e3a-4526-a10c-1c8a418bc280"}}))
  )  