(ns sanatoriocolegiales.logging-servoy.router
  "API global request routing"
  (:require
   ;; Core Web Application Libraries
   [reitit.ring   :as ring]
   [muuntaja.core :as muuntaja]
   ;; Routing middleware
   [reitit.ring.middleware.muuntaja   :as middleware-muuntaja]
   [reitit.ring.middleware.parameters :as parameters]
   ;; [reitit.ring.middleware.exception  :as exception]
   ;; Service middleware
   [sanatoriocolegiales.logging-servoy.middleware :as middleware-service]
   ;; Service Routing
   [sanatoriocolegiales.logging-servoy.api.system-admin :as system-admin]
   [sanatoriocolegiales.logging-servoy.api.logger   :as logger]
   ;; Self-documenting API
   [reitit.swagger    :as api-docs]
   [reitit.swagger-ui :as api-docs-ui]
   ;; Provide details of parameters to API documentation UI (swagger) 
   [reitit.ring.coercion :as coercion]
   [malli.util :as mu]
   [reitit.coercion.malli]
   [reitit.ring.malli]
   [reitit.ring.spec :as spec]
   ;; Error handling
   [reitit.dev.pretty :as pretty]
   ; Event Logging
   [com.brunobonacci.mulog :as mulog]))

(def open-api-docs
  "Open API docs general information about the service,
  https://practical.li/clojure-web-services/project/gameboard/
  keys composed of multiple names should use camelCase"
  ["/swagger.json"
   {:get {:no-doc  true
          :swagger {:info {:title "sanatoriocolegiales logging-servoy Service API"
                           :description "Servicio para loggear eventos de la aplicación Servoy"
                           :version "0.1.0"}}
          :handler (api-docs/create-swagger-handler)}}])

(def router-configuration
  "Reitit configuration of coercion, data format transformation and middleware for all routing"
  {:data {:coercion (reitit.coercion.malli/create
                     {;; set of keys to include in error messages
                      :error-keys #{#_:type :coercion :in :schema :value :errors :humanized #_:transformed}
           ;; schema identity function (default: close all map schemas)
                      :compile mu/closed-schema
           ;; strip-extra-keys (affects only predefined transformers)
                      :strip-extra-keys true
           ;; add/set default values
                      :default-values true
           ;; malli options
                      :options nil})
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
   :validate spec/validate})

(defn app
  [db]
  (mulog/log ::router-app :system-config db)
  (ring/ring-handler
   (ring/router
    [open-api-docs
     (system-admin/routes)
     ["/api"
      ["/v1"
       (logger/routes db)]]]
    router-configuration)
   (ring/routes
    (api-docs-ui/create-swagger-ui-handler {:path "/"})
    (ring/create-default-handler))))
