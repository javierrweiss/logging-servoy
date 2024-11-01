;; ---------------------------------------------------------
;; Donut System REPL
;;
;; Tools for REPl workflow with Donut system components
;; ---------------------------------------------------------

(ns system-repl
  "Tools for REPl workflow with Donut system components"
  (:require
   [donut.system :as donut]
   [donut.system.repl :as donut-repl]
   [donut.system.repl.state :as donut-repl-state]
   [sanatoriocolegiales.logging-servoy.system :as system]
   [com.brunobonacci.mulog :as mulog]
   [reitit.ring   :as ring]
   [sanatoriocolegiales.logging-servoy.api.system-admin :as system-admin]
   [reitit.swagger    :as api-docs]
   [reitit.swagger-ui :as api-docs-ui]
   [sanatoriocolegiales.logging-servoy.api.logger   :as logger]
   [reitit.ring.spec :as rrs]
   [reitit.dev.pretty :as pretty]
    [reitit.ring.middleware.muuntaja   :as middleware-muuntaja]
   [reitit.ring.middleware.parameters :as parameters]
   [sanatoriocolegiales.logging-servoy.middleware :as middleware-service]
   [reitit.coercion.spec]
   [reitit.ring.coercion :as coercion]
   [muuntaja.core :as muuntaja]))

(def dev-open-api-docs
  "Open API docs general information about the service,
  https://practical.li/clojure-web-services/project/gameboard/
  keys composed of multiple names should use camelCase"
  ["/swagger.json"
   {:get {:no-doc  true
          :swagger {:info {:title "sanatoriocolegiales logging-servoy Service API"
                           :description "Servicio para loggear eventos de la aplicación Servoy"
                           :version "0.1.0"}}
          :handler (api-docs/create-swagger-handler)}}])

(def dev-router-configuration
  "Reitit configuration of coercion, data format transformation and middleware for all routing"
  {:data {:coercion   reitit.coercion.spec/coercion
          :muuntaja   muuntaja/instance
          :middleware [;; swagger feature for OpenAPI documentation
                       api-docs/swagger-feature
                       ;; query-params & form-params
                       parameters/parameters-middleware
                       ;; content-negotiation
                       middleware-muuntaja/format-middleware
                       ;; excepciones
                       middleware-service/exception-middleware
                       ;; coercing response bodys
                       coercion/coerce-response-middleware
                       ;; coercing request parameters
                       coercion/coerce-request-middleware
                       ;; Pretty print exceptions
                       coercion/coerce-exceptions-middleware
                       ;; logging with mulog
                       [middleware-service/wrap-trace-events :trace-events]]}
   ;; pretty-print reitit exceptions for human consumptions
   :exception pretty/exception
   :validate rrs/validate})
;;https://cljdoc.org/d/metosin/reitit/0.7.2/doc/advanced/dev-workflow
(defn dev-app
  [db]
  (mulog/log ::router-app :system-config db)
  (ring/ring-handler
   #(ring/router
    [dev-open-api-docs
     (system-admin/routes)
     ["/api"
      ["/v1"
       (logger/routes db)]]]
    dev-router-configuration)
   (ring/routes
    (api-docs-ui/create-swagger-ui-handler {:path "/"})
    (ring/create-default-handler))))

;; ---------------------------------------------------------
;; Donut named systems
;; `:donut.system/repl` is default named system,
;; bound to `sanatoriocolegiales.logging-servoy.system` configuration
(defmethod donut/named-system :donut.system/repl
  [_] system/main)

;; `dev` system, partially overriding main system configuration
;; to support the development workflow
(defmethod donut/named-system :dev
  [_] (donut/system :donut.system/repl
                    {[:env :app-env] "dev"
                     [:env :app-version] "0.0.0-SNAPSHOT"
                     [:services :http-server ::donut/config :options :join?] false 
                     ;; Capaz no entendí la configuración, pero me rompió todo.
                     #_#_[:http :handler ::donut/start] (fn inicia-handler-dev
                                                      [{{:keys [db-obj]} ::donut/config}]
                                                      (dev-app db-obj))
                     [:services :event-log-publisher ::donut/config] {:publisher {:type :console :pretty? true}}}))
;; ---------------------------------------------------------

;; ---------------------------------------------------------
;; Donut REPL workflow helper functions

(defn start
  "Start services using a named-system configuration,
  use `:dev` named-system by default"
  ([] (start :dev))
  ([named-system] (donut-repl/start named-system)))

(defn stop
  "Stop the currently running system"
  []  (donut-repl/stop))

(defn restart
  "Restart the system with donut repl,
  Uses clojure.tools.namespace.repl to reload namespaces
  `(clojure.tools.namespace.repl/refresh :after 'donut.system.repl/start)`"
  [] (donut-repl/restart))

(defn system
  "Return: fully qualified hash-map of system state"
  [] donut-repl-state/system)
;; ---------------------------------------------------------
