(ns sanatoriocolegiales.transaccion-test
  (:require [sanatoriocolegiales.logging-servoy.persistence.transaccion :refer :all]
            [clojure.test :refer [deftest is run-all-tests run-test testing]]
            [matcher-combinators.test]
            [matcher-combinators.matchers :as m]))

(deftest funciones-utilitarias-devuelven-mapa-con-la-estructura-esperada
  (testing "Cuando actualizar-atributo-esquema recibe dos llave calificadas, devuelve mapa correcto"
    (is (match? {:db/id :llave/xyz
                 :db/ident :llave/buena}
                (actualizar-atributo-esquema :llave/xyz :llave/buena))))
  (testing "Cuando actualizar-atributo-esquema no recibe alguna llave calificada, lanza excepción"
    (is (thrown? java.lang.IllegalArgumentException (actualizar-atributo-esquema :llave :llave/buena)))
    (is (thrown? java.lang.IllegalArgumentException (actualizar-atributo-esquema :llave/mala :llave))))
  (testing "Cuando actualizar-atributo-esquema algún valor que no es llave, lanza excepción"
    (is (thrown? java.lang.IllegalArgumentException (actualizar-atributo-esquema :llave/mala nil)))
    (is (thrown? java.lang.IllegalArgumentException (actualizar-atributo-esquema nil nil)))
    (is (thrown? java.lang.IllegalArgumentException (actualizar-atributo-esquema 'cachicamo true)))
    (is (thrown? java.lang.IllegalArgumentException (actualizar-atributo-esquema "" 22323))))
  (testing "Cuando agregar-nuevo-atributo recibe un ident que no es llave calificada o no es llave, lanza excepción"
    (is (thrown? java.lang.IllegalArgumentException (agregar-nuevo-atributo :dado 'v 'y 'd false)))
    (is (thrown? java.lang.IllegalArgumentException (agregar-nuevo-atributo "dado" 'v 'y 'd false)))
    (is (thrown? java.lang.IllegalArgumentException (agregar-nuevo-atributo 'dado 'v 'y 'd false)))
    (is (thrown? java.lang.IllegalArgumentException (agregar-nuevo-atributo "tes" 'v 'y 'd false))) 
    (is (thrown? java.lang.IllegalArgumentException (agregar-nuevo-atributo 154.001 'v 'y 'd false))))
  (testing "Cuando agregar-nuevo-atributo recibe un tipo de dato que no es llave calificada o no se corresponde a la especificación, lanza excepción"
    (is (thrown? java.lang.IllegalArgumentException (agregar-nuevo-atributo :dado/seis :alfa 'y 'd false)))
    (is (thrown? java.lang.IllegalArgumentException (agregar-nuevo-atributo :dado/seis :string 'y 'd false)))
    (is (thrown? java.lang.IllegalArgumentException (agregar-nuevo-atributo :dado/seis :type/string 'y 'd false))))
  (testing "Cuando agregar-nuevo-atributo recibe una cardinalidad inexistente o una llave que no es calificada, lanza excepción"
    (is (thrown? java.lang.IllegalArgumentException (agregar-nuevo-atributo :dado/seis :db.type/string 'y 'd false)))
    (is (thrown? java.lang.IllegalArgumentException (agregar-nuevo-atributo :dado/seis :db.type/string :y 'd false)))
    (is (thrown? java.lang.IllegalArgumentException (agregar-nuevo-atributo :dado/seis :db.type/string :alfa/beta 'd false))))
  (testing "Cuando agregar-nuevo-atributo recibe documentación nula o que no es string, lanza excepción"
    (is (thrown? java.lang.IllegalArgumentException (agregar-nuevo-atributo :dado/seis :db.type/string :db.cardinality/one 'd false)))
    (is (thrown? java.lang.IllegalArgumentException (agregar-nuevo-atributo :dado/seis :db.type/string :db.cardinality/one 1000 false)))
    (is (thrown? java.lang.IllegalArgumentException (agregar-nuevo-atributo :dado/seis :db.type/string :db.cardinality/one nil false)))
    (is (thrown? java.lang.IllegalArgumentException (agregar-nuevo-atributo :dado/seis :db.type/string :db.cardinality/one true false))))
  (testing "Cuando agregar-nuevo-atributo recibe argument <unico?> que no es booleano, lanza excepción"
    (is (thrown? java.lang.IllegalArgumentException (agregar-nuevo-atributo :dado/seis :db.type/string :db.cardinality/one "string" nil)))
    (is (thrown? java.lang.IllegalArgumentException (agregar-nuevo-atributo :dado/seis :db.type/string :db.cardinality/one "string" :nil)))
    (is (thrown? java.lang.IllegalArgumentException (agregar-nuevo-atributo :dado/seis :db.type/string :db.cardinality/one "string" 2332)))
    (is (thrown? java.lang.IllegalArgumentException (agregar-nuevo-atributo :dado/seis :db.type/string :db.cardinality/one "string" "bcims"))))
  (testing "Cuando agregar-nuevo-atributo recibe argumentos correctos, devuelve mapa con la forma esperada"
    (is (match? {:db/ident :evento/cirugia
                 :db/valueType :db.type/string
                 :db/cardinality :db.cardinality/one
                 :db/doc "Esta es una documentación"} 
                (agregar-nuevo-atributo :evento/cirugia :db.type/string :db.cardinality/one "Esta es una documentación" false)))
    (is (match? {:db/ident :paciente/tipo
                 :db/valueType :db.type/ref
                 :db/cardinality :db.cardinality/one
                 :db/doc "Esta es una documentación"
                 :db/unique :db.unique/identity}
                (agregar-nuevo-atributo :paciente/tipo :db.type/ref :db.cardinality/one "Esta es una documentación" true)))))  

 
(comment 
  
  (run-test funciones-utilitarias-devuelven-mapa-con-la-estructura-esperada)
  
  )
