(ns sanatoriocolegiales.logging-servoy.service-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [donut.system :as system]
            [org.httpkit.client :as client]))

(use-fixtures :once (fn [f]
                      (system/start :test)
                      (f)
                      (system/stop :test)))

(def url "http://localhost:2500/api/v1")

(deftest service-test
  (testing "Cuando recibe una solicitud con el método post al endpoint /cirugia devuelve 201 cuando el recurso fue creado")
  (testing "Cuando recibe una solicitud con el método post al endpoint /cirugia devuelve 400 cuando el body no tiene la forma correcta")
  (testing "Cuando recibe una solicitud con el método delete al endpoint /cirugia devuelve 200 cuando fue exitoso")
  (testing "Cuando recibe una solicitud con el método delete al endpoint /cirugia devuelve 404 cuando el recurso no existe")
  (testing "Cuando recibe una solicitud con el método put al endpoint /cirugia devuelve 201 cuando el recurso fue actualizado")
  (testing "Cuando recibe una solicitud con el método put al endpoint /cirugia devuelve 204 cuando ya se recibió la misma petición de actualización")
  (testing "Cuando recibe una solicitud con el método put al endpoint /cirugia devuelve 400 cuando la petición es inválida")
  (testing "Cuando recibe una solicitud con el método patch al endpoint /cirugia devuelve 204 cuando el recurso fue actualizado")
  (testing "Cuando recibe una solicitud con el método put al endpoint /cirugia devuelve 400 cuando la petición es inválida")
  (testing "Cuando recibe una solicitud con el método post al endpoint /convenios devuelve 201 cuando el recurso fue creado")
  (testing "Cuando recibe una solicitud con el método post al endpoint /convenios devuelve 400 cuando el body no tiene la forma correcta")
  (testing "Cuando recibe una solicitud con el método delete al endpoint /convenios devuelve 200 cuando el recurso fue borrado")
  (testing "Cuando recibe una solicitud con el método delete al endpoint /convenios devuelve 404 cuando el recurso no se encontró")
  (testing "Cuando recibe una solicitud con el método put al endpoint /convenios devuelve 201 cuando el recurso fue actualizado")
  (testing "Cuando recibe una solicitud con el método put al endpoint /convenios devuelve 204 cuando ya se recibió la misma petición de actualización")
  (testing "Cuando recibe una solicitud con el método patch al endpoint /convenios devuelve 204 cuando el recurso fue actualizado")
  (testing "Cuando recibe una solicitud correcta con el método get al endpoint /excepciones_desde devuelve 200")
  (testing "Cuando recibe una solicitud incorrecta con el método get al endpoint /excepciones_desde devuelve 400")
  (testing "Cuando recibe una solicitud correcta con el método get al endpoint /excepciones_origen devuelve 200")
  (testing "Cuando recibe una solicitud incorrecta con el método get al endpoint /excepciones_origen devuelve 400")
  (testing "Cuando recibe una solicitud correcta con el método get al endpoint /eventos_por_hc devuelve 200")
  (testing "Cuando recibe una solicitud incorrecta con el método get al endpoint /eventos_por_hc devuelve 400")
  (testing "Cuando recibe una solicitud correcta con el método get al endpoint /eventos_por_hcu devuelve 200")
  (testing "Cuando recibe una solicitud incorrecta con el método get al endpoint /eventos_por_hcu devuelve 400")
  (testing "Cuando recibe una solicitud correcta con el método get al endpoint /evento devuelve 200")
  (testing "Cuando recibe una solicitud incorrecta con el método get al endpoint /evento devuelve 400")
  (testing "Cuando recibe una solicitud correcta con el método get al endpoint /todos_eventos devuelve 200"))


(comment
  (clojure.test/run-all-tests)
  )